package com.hyh.plg.service;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.hyh.plg.api.BlockEnv;
import com.hyh.plg.utils.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tangdongwei on 2018/11/20.
 */
public class BlockServiceServer implements IServiceServer {

    public static final String SERVICE_PROXY_CLASS_PATH = "service_proxy_class_path";

    @SuppressLint("StaticFieldLeak")
    private static BlockServiceServer mInstance;

    private Context mContext;

    private final Map<String, BlockServiceRecord> mServices;

    private final List<Integer> mConnectionHashList;

    private final SparseArray<List<BlockServiceRecord>> mConnectionServiceRecordArray;

    private final ServiceHandler mServiceHandler;

    private BlockServiceServer(Context context) {
        this.mContext = context;
        this.mServices = new HashMap<>();
        this.mConnectionHashList = new CopyOnWriteArrayList<>();
        this.mConnectionServiceRecordArray = new SparseArray<>();
        this.mServiceHandler = new ServiceHandler();
    }

    public static BlockServiceServer getInstance(Context context) {
        if (mInstance == null) {
            synchronized (BlockServiceServer.class) {
                if (mInstance == null) {
                    mInstance = new BlockServiceServer(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    @Override
    public ComponentName startService(Intent service) {
        if (scheduleStartServiceLocked(service)) {
            return service.getComponent();
        } else {
            return null;
        }
    }

    @Override
    public ComponentName startForegroundService(Intent service) {
        if (scheduleStartServiceLocked(service)) {
            return service.getComponent();
        } else {
            return null;
        }
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return scheduleBindServiceLocked(service, conn, flags);
    }

    @Override
    public boolean unbindService(ServiceConnection conn) {
        return scheduleUnbindServiceLocked(conn);
    }

    @Override
    public boolean stopService(Intent name) {
        return scheduleStopServiceLocked(name);
    }

    /**
     * 安排执行插件service的create和start流程
     *
     * @param service
     * @return false=不执行。true=执行
     */
    private synchronized boolean scheduleStartServiceLocked(Intent service) {
        //拿到插件service的class
        Class serviceClass = getBlockServiceClass(service);
        Logger.d("IN BlockServiceServer, scheduleStartServiceLocked, serviceClass = " + serviceClass);
        if (serviceClass == null) {
            return false;
        }
        mServiceHandler.postStartService(service, serviceClass);
        return true;
    }

    /**
     * 安排执行插件service的bind流程
     *
     * @return false=不执行。true=执行
     */
    private synchronized boolean scheduleBindServiceLocked(Intent service, ServiceConnection conn, int flags) {
        if (service == null || conn == null) {
            return false;
        }
        Class serviceClass = getBlockServiceClass(service);
        Logger.d("IN BlockServiceServer, scheduleBindServiceLocked, serviceClass = " + serviceClass);
        if (serviceClass == null) {
            return false;
        }
        mConnectionHashList.add(System.identityHashCode(conn));
        mServiceHandler.postBindService(service, conn, serviceClass);
        return true;
    }

    private synchronized boolean scheduleUnbindServiceLocked(ServiceConnection conn) {
        if (conn == null) return false;
        Integer connHashCode = System.identityHashCode(conn);
        if (!mConnectionHashList.contains(connHashCode)) return false;
        mConnectionHashList.remove(connHashCode);
        mServiceHandler.postUnbindService(conn);
        return true;
    }

    private synchronized boolean scheduleStopServiceLocked(Intent name) {
        Class serviceClass = getBlockServiceClass(name);
        Logger.d("IN BlockServiceServer, scheduleStopServiceLocked, serviceClass = " + serviceClass);
        if (serviceClass == null) {
            return false;
        }
        mServiceHandler.postStopService(name, serviceClass);
        return true;
    }


    private void performStartService(Intent service, Class serviceClass) {
        BlockServiceRecord serviceRecord = mServices.get(serviceClass.getName());
        Logger.d("performStartService, serviceRecord = " + serviceRecord);
        if (serviceRecord == null) {
            Service serviceInstance = newServiceInstance(serviceClass);
            if (serviceInstance == null) {
                return;
            }
            fixServiceContext(serviceInstance);

            serviceRecord = new BlockServiceRecord();
            serviceRecord.setServiceInstance(serviceInstance);

            onServiceCreate(serviceRecord);
            onServiceStartCommand(serviceRecord, service);
        } else {
            onServiceStartCommand(serviceRecord, service);
        }
    }

    private void performBindService(Intent service, ServiceConnection connection, Class serviceClass) {
        BlockServiceRecord serviceRecord = mServices.get(serviceClass.getName());
        Logger.d("performBindService, serviceRecord = " + serviceRecord);
        if (serviceRecord == null) {
            Service serviceInstance = newServiceInstance(serviceClass);
            Logger.d("performBindService, serviceInstance = " + serviceInstance);
            if (serviceInstance == null) {
                return;
            }
            fixServiceContext(serviceInstance);
            serviceRecord = new BlockServiceRecord();
            serviceRecord.setServiceInstance(serviceInstance);

            onServiceCreate(serviceRecord);
            onServiceBind(serviceRecord, service, connection);
        } else {
            onServiceBind(serviceRecord, service, connection);
        }
    }

    private void performUnbindService(ServiceConnection connection) {
        Integer connHashCode = System.identityHashCode(connection);
        List<BlockServiceRecord> serviceRecords = mConnectionServiceRecordArray.get(connHashCode);
        if (serviceRecords == null || serviceRecords.isEmpty()) return;
        mConnectionServiceRecordArray.remove(connHashCode);
        for (BlockServiceRecord serviceRecord : serviceRecords) {
            performUnbindService(serviceRecord, connHashCode);
        }
    }

    private void performUnbindService(BlockServiceRecord serviceRecord, Integer connHashCode) {
        BinderRecord binderRecord = serviceRecord.getBinderRecord();
        boolean isConnectionEmpty = true;
        if (binderRecord != null && binderRecord.connectionIntentCache.size() > 0) {
            Intent connectionIntent = binderRecord.connectionIntentCache.get(connHashCode);
            binderRecord.connectionIntentCache.remove(connHashCode);
            if (connectionIntent != null) {
                onServiceUnbind(serviceRecord, connectionIntent);
            }
            isConnectionEmpty = binderRecord.connectionIntentCache.size() == 0;
        }
        //只有当Service没有被执行start，且没有链接对象时，才能执行onDestroy
        if (!serviceRecord.isStarted() && isConnectionEmpty) {
            onServiceDestroy(serviceRecord);
        }
    }

    private void performStopService(Class serviceClass) {
        BlockServiceRecord serviceRecord = mServices.get(serviceClass.getName());
        if (serviceRecord == null) {
            return;
        }
        serviceRecord.setStarted(false);
        BinderRecord binderRecord = serviceRecord.getBinderRecord();

        //只有当Service没有被执行start，且没有链接对象时，才能执行onDestroy
        if (binderRecord == null || binderRecord.connectionIntentCache.size() == 0) {
            onServiceDestroy(serviceRecord);
        }
    }

    private void onServiceCreate(BlockServiceRecord serviceRecord) {
        Service serviceInstance = serviceRecord.getServiceInstance();
        mServices.put(serviceInstance.getClass().getName(), serviceRecord);
        serviceInstance.onCreate();

        Logger.d("onServiceCreate: serviceRecord = " + serviceRecord);
    }

    private void onServiceStartCommand(BlockServiceRecord serviceRecord, Intent service) {
        serviceRecord.setStarted(true);
        Service serviceInstance = serviceRecord.getServiceInstance();
        int startId = serviceRecord.getStartId();
        startId += 1;
        serviceRecord.setStartId(startId);
        serviceInstance.onStartCommand(service, 0, startId);

        Logger.d("onServiceStartCommand: serviceRecord = " + serviceRecord);
    }

    private void onServiceBind(BlockServiceRecord serviceRecord, Intent service, ServiceConnection connection) {
        Integer connHashCode = System.identityHashCode(connection);

        List<BlockServiceRecord> serviceRecords = mConnectionServiceRecordArray.get(connHashCode);
        if (serviceRecords == null) {
            serviceRecords = new ArrayList<>();
            serviceRecords.add(serviceRecord);
            mConnectionServiceRecordArray.put(connHashCode, serviceRecords);
        } else {
            if (!serviceRecords.contains(serviceRecord)) {
                serviceRecords.add(serviceRecord);
            }
        }

        Intent.FilterComparison comparison = new Intent.FilterComparison(service);

        Service serviceInstance = serviceRecord.getServiceInstance();
        BinderRecord binderRecord = serviceRecord.getBinderRecord();

        if (binderRecord != null) {//这个Service之前被绑定过


            //看看这个Intent有没有缓存过IBinder，如果已经缓存了IBinder，就不需要调用Service的onBind方法获取IBinder了
            boolean hasCacheBinder = binderRecord.binderCache.containsKey(comparison);

            //以Intent为Key，取出缓存的IBinder对象
            IBinder cacheBinder = binderRecord.binderCache.get(comparison);

            IBinder currentBinder = null;
            if (!hasCacheBinder) {
                //没有缓存IBinder，说明这个Intent是第一次bindService
                currentBinder = serviceInstance.onBind(service);
                Logger.d("onServiceBind: binder from service = " + currentBinder);
                binderRecord.binderCache.put(comparison, currentBinder);
            }

            //看看这个connection有没有bind过这个Service
            Intent connectionIntent = binderRecord.connectionIntentCache.get(connHashCode);
            if (connectionIntent != null) {//有缓存的Intent，说明该connection bind过这个Service
                //如果当前的请求的Intent与缓存的Intent的不一致，则先onServiceDisconnected再onServiceConnected；
                //如果一致，那么说明这个ServiceConnection已经通过这个Intent绑定了这个服务，所以这里什么都不需要干。
                if (!connectionIntent.filterEquals(service)) {
                    binderRecord.connectionIntentCache.put(connHashCode, service);

                    //当缓存的IBinder对象为null时，说明上次bindService时没有调用onServiceConnected方法，所以这里也不用调用onServiceConnected
                    if (cacheBinder != null) {
                        connection.onServiceDisconnected(connectionIntent.getComponent());
                    }

                    //如果从Service#onBind方法中得到的IBinder对象为null，那么就不需要调用onServiceConnected方法
                    if (currentBinder != null) {
                        connection.onServiceConnected(service.getComponent(), currentBinder);
                    }
                }
            } else {
                binderRecord.connectionIntentCache.put(connHashCode, service);

                //如果从Service#onBind方法中得到的IBinder对象为null，那么就不需要调用onServiceConnected方法
                if (currentBinder != null) {
                    connection.onServiceConnected(service.getComponent(), currentBinder);
                }
            }

        } else {//这个Service第一次被绑定
            binderRecord = new BinderRecord();
            IBinder binder = serviceInstance.onBind(service);
            Logger.d("onServiceBind: binder from service = " + binder);
            binderRecord.binderCache.put(comparison, binder);
            binderRecord.connectionIntentCache.put(connHashCode, service);
            serviceRecord.setBinderRecord(binderRecord);
            if (binder != null) {
                connection.onServiceConnected(service.getComponent(), binder);
            }
        }
    }

    private void onServiceUnbind(BlockServiceRecord serviceRecord, Intent connectionIntent) {
        serviceRecord.getServiceInstance().onUnbind(connectionIntent);
    }

    private void onServiceDestroy(BlockServiceRecord serviceRecord) {
        Service serviceInstance = serviceRecord.getServiceInstance();
        mServices.remove(serviceInstance.getClass().getName());
        serviceInstance.onDestroy();
    }

    private Class getBlockServiceClass(Intent service) {
        try {
            if (service == null) {
                return null;
            }
            if (service.getComponent() == null) {
                return null;
            } else {
                //load一把，如果用插件的classloader能找到这个service，就认为是插件的。虽然有瑕疵，但是也没办法
                ComponentName component = service.getComponent();
                String className = component.getClassName();
                try {
                    return getClass().getClassLoader().loadClass(className);
                } catch (Exception e) {
                    Logger.d("IN BlockServiceServer, getBlockServiceClass, can not load " + className + " , it is not belong to block");
                    return null;
                }
            }
        } catch (Exception e) {
            Logger.e("IN BlockServiceServer, getBlockServiceClass ERROR : " + e.toString());
        }
        return null;
    }

    private Service newServiceInstance(Class blockServiceClass) {
        try {
            return (Service) blockServiceClass.newInstance();
        } catch (Exception e) {
            Logger.e("IN BlockServiceServer, newServiceInstance (" + blockServiceClass + ") failed ", e);
        }
        return null;
    }

    /**
     * 修复service的上下文变量
     *
     * @param service
     */
    private void fixServiceContext(Service service) {
        if (service == null) {
            return;
        }
        Context baseContext = getBlockServiceBaseContext(service);
        //fix Application
        try {
            Field mApplicationField = Service.class.getDeclaredField("mApplication");
            mApplicationField.setAccessible(true);
            mApplicationField.set(service, baseContext);
        } catch (Exception e) {
            Logger.e("IN BlockServiceServer, fixServiceContext(), fix Application ERROR : " + e.toString());
        }
        //fix IActivityManager
        try {
            Field mActivityManager = Service.class.getDeclaredField("mActivityManager");
            mActivityManager.setAccessible(true);
            mActivityManager.set(service, IActivityManagerProxyForService.create(mContext));
        } catch (Exception e) {
            Logger.e("IN BlockServiceServer, fixServiceContext(), fix IActivityManager ERROR : " + e.toString());
        }
        //fix className
        try {
            Field mClassName = Service.class.getDeclaredField("mClassName");
            mClassName.setAccessible(true);
            mClassName.set(service, service.getClass().getName());
        } catch (Exception e) {
            Logger.e("IN BlockServiceServer, fixServiceContext(), fix className ERROR : " + e.toString());
        }
        //fix mBase
        try {
            Field mBase = ContextWrapper.class.getDeclaredField("mBase");
            mBase.setAccessible(true);
            mBase.set(service,baseContext);
        } catch (Exception e) {
            Logger.e("IN BlockServiceServer, fixServiceContext(), fix ContextWrapper mBase ERROR : " + e.toString());
        }
        // TODO: 2018/11/21 fix mToken
        // TODO: 2018/11/21 fix ActivityThread
    }

    private Context getBlockServiceBaseContext(Service service) {
        Context context = BlockEnv.sBlockServiceBaseContextMap.get(service.getClass().getName());
        if (context == null) return BlockEnv.sBlockApplication;
        return context;
    }


    @SuppressLint("HandlerLeak")
    private class ServiceHandler extends Handler {

        private static final int HANDLE_START_SERVICE = 1;
        private static final int HANDLE_BIND_SERVICE = 2;
        private static final int HANDLE_UNBIND_SERVICE = 3;
        private static final int HANDLE_STOP_SERVICE = 4;

        ServiceHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg == null) return;
            Object obj = msg.obj;
            if (obj == null || !(obj instanceof ServiceCaller)) return;
            ServiceCaller serviceCaller = (ServiceCaller) obj;
            switch (msg.what) {
                case HANDLE_START_SERVICE: {
                    performStartService(serviceCaller.callerIntent, serviceCaller.serviceClass);
                    break;
                }
                case HANDLE_BIND_SERVICE: {
                    performBindService(serviceCaller.callerIntent, serviceCaller.connection, serviceCaller.serviceClass);
                    break;
                }
                case HANDLE_UNBIND_SERVICE: {
                    performUnbindService(serviceCaller.connection);
                    break;
                }
                case HANDLE_STOP_SERVICE: {
                    performStopService(serviceCaller.serviceClass);
                    break;
                }
            }
        }

        void postStartService(Intent service, Class serviceClass) {
            Message message = obtainMessage(HANDLE_START_SERVICE);
            ServiceCaller serviceCaller = new ServiceCaller();
            serviceCaller.callerIntent = service;
            serviceCaller.serviceClass = serviceClass;
            message.obj = serviceCaller;
            sendMessage(message);
        }

        void postBindService(Intent service, ServiceConnection conn, Class serviceClass) {
            Message message = obtainMessage(HANDLE_BIND_SERVICE);
            ServiceCaller serviceCaller = new ServiceCaller();
            serviceCaller.callerIntent = service;
            serviceCaller.connection = conn;
            serviceCaller.serviceClass = serviceClass;
            message.obj = serviceCaller;
            sendMessage(message);
        }

        void postUnbindService(ServiceConnection conn) {
            Message message = obtainMessage(HANDLE_UNBIND_SERVICE);
            ServiceCaller serviceCaller = new ServiceCaller();
            serviceCaller.connection = conn;
            message.obj = serviceCaller;
            sendMessage(message);
        }

        void postStopService(Intent name, Class serviceClass) {
            Message message = obtainMessage(HANDLE_STOP_SERVICE);
            ServiceCaller serviceCaller = new ServiceCaller();
            serviceCaller.callerIntent = name;
            serviceCaller.serviceClass = serviceClass;
            message.obj = serviceCaller;
            sendMessage(message);
        }
    }

    private static class ServiceCaller {
        Intent callerIntent;
        ServiceConnection connection;
        Class serviceClass;
    }
}