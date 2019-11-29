package com.hyh.arithmetic.linked;

/**
 * @author Administrator
 * @description
 * @data 2019/11/29
 */
public class LinkedNum {

    private NumNode first;

    private int size;

    public LinkedNum(int original) {
        int size = 0;
        if (original <= 0) {
            this.size = 1;
            this.first = new NumNode(0, null);
            return;
        }
        NumNode next = null;
        while (original > 0) {
            int mod = original % 10;
            original = original / 10;
            next = new NumNode(mod, next);
            size++;
        }
        this.first = next;
        this.size = size;
    }

    public void addNum(LinkedNum linkedNum) {
        int otherIndex = linkedNum.size - 1;
        NumNode otherNumNode = linkedNum.get(otherIndex);
        int carryDigit = 0;

        while (otherNumNode != null) {
            NumNode thisNumNode = null;
            int thisIndex = this.size - (linkedNum.size - otherIndex) - carryDigit;
            if (thisIndex >= 0) {
                thisNumNode = this.get(thisIndex);
            }

            if (thisNumNode == null) {
                NumNode oldFirst = this.first;
                this.first = new NumNode(otherNumNode.num, oldFirst);
                size++;
                carryDigit = 0;
                otherIndex--;
                if (otherIndex >= 0) {
                    otherNumNode = linkedNum.get(otherIndex);
                } else {
                    otherNumNode = null;
                }
                continue;
            }

            int addNum = thisNumNode.num + otherNumNode.num;
            if (addNum >= 10) {
                carryDigit++;
                thisNumNode.num = addNum % 10;
                otherNumNode = new NumNode(addNum / 10, null);
            } else {
                carryDigit = 0;
                thisNumNode.num = addNum;
                otherIndex--;
                if (otherIndex >= 0) {
                    otherNumNode = linkedNum.get(otherIndex);
                } else {
                    otherNumNode = null;
                }
            }
        }
    }

    private NumNode get(int index) {
        if (index >= size) throw new ArrayIndexOutOfBoundsException();
        NumNode numNode = first;
        for (int i = 0; i < index; i++) {
            numNode = numNode.next;
        }
        return numNode;
    }


    @Override
    public String toString() {
        int num = 0;
        int index = 0;
        NumNode numNode = first;
        while (numNode != null) {
            num += numNode.num * Math.pow(10, size - 1 - index);
            numNode = numNode.next;
            index++;
        }
        return String.valueOf(num);
    }

    private static class NumNode {

        int num;

        NumNode next;

        NumNode(int num, NumNode next) {
            this.num = num;
            this.next = next;
        }
    }
}