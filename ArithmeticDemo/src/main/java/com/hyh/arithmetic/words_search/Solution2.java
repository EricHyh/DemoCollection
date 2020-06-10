package com.hyh.arithmetic.words_search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2020/6/9
 */
public class Solution2 {

    /**
     * [["a","b"],["c","d"]]","rain"] and board =
     * ["acdb"]
     */
    public List<String> findWords(char[][] board, String[] words) {
        List<String> results = new ArrayList<>();
        Trie trie = new Trie();
        for (String word : words) {
            trie.insert(word);
        }
        Node root = trie.root;
        List<Character> linkValues = root.linkValues;
        int xlen = board[0].length;
        int ylen = board.length;
        for (Character linkValue : linkValues) {
            match(board, xlen, ylen, root.get(linkValue), null, null, null, results);
        }
        return results;
    }


    private boolean match(char[][] board, int xlen, int ylen,
                          Node node,
                          int[] lastHeadPosition, int[] lastPosition,
                          LinkedList<int[]> positions,
                          List<String> results) {
        if (lastPosition == null) {
            int[] position = findPosition(board, xlen, ylen, node.val, lastHeadPosition);
            if (position == null) return false;

            positions = new LinkedList<>();
            positions.add(position);
            boolean match = match(board, xlen, ylen, node, lastHeadPosition, position, positions, results);
            if (match) {
                return true;
            } else {
                lastHeadPosition = position;
                return match(board, xlen, ylen, node, lastHeadPosition, null, null, results);
            }
        } else {
            if (node.isEnd() && !results.contains(node.word)) {
                results.add(node.word);
            }

            List<Character> linkValues = node.linkValues;
            if (linkValues.isEmpty()) return false;

            int lastX = lastPosition[0];
            int lastY = lastPosition[1];

            boolean result = false;
            for (Character linkValue : linkValues) {
                Node nextNode = node.get(linkValue);
                {
                    int lx = lastX - 1;
                    int ly = lastY;
                    if (lx >= 0 && board[ly][lx] == linkValue) {
                        int[] position = {lx, ly};
                        if (!contains(positions, position)) {
                            if (nextNode != null) {
                                LinkedList<int[]> newPositions = new LinkedList<>(positions);
                                newPositions.add(position);
                                boolean match = match(board, xlen, ylen, nextNode, lastHeadPosition, position, newPositions, results);
                                if (match) {
                                    result = true;
                                }
                            }
                        }
                    }
                }

                {
                    int tx = lastX;
                    int ty = lastY - 1;
                    if (ty >= 0 && board[ty][tx] == linkValue) {
                        int[] position = {tx, ty};
                        if (!contains(positions, position)) {
                            if (nextNode != null) {
                                LinkedList<int[]> newPositions = new LinkedList<>(positions);
                                newPositions.add(position);
                                boolean match = match(board, xlen, ylen, nextNode, lastHeadPosition, position, newPositions, results);
                                if (match) {
                                    result = true;
                                }
                            }
                        }
                    }
                }

                {
                    int rx = lastX + 1;
                    int ry = lastY;
                    if (rx < xlen && board[ry][rx] == linkValue) {
                        int[] position = {rx, ry};
                        if (!contains(positions, position)) {
                            if (nextNode != null) {
                                LinkedList<int[]> newPositions = new LinkedList<>(positions);
                                newPositions.add(position);
                                boolean match = match(board, xlen, ylen, nextNode, lastHeadPosition, position, newPositions, results);
                                if (match) {
                                    result = true;
                                }
                            }
                        }
                    }
                }

                {
                    int bx = lastX;
                    int by = lastY + 1;
                    if (by < ylen && board[by][bx] == linkValue) {
                        int[] position = {bx, by};
                        if (!contains(positions, position)) {
                            if (nextNode != null) {
                                LinkedList<int[]> newPositions = new LinkedList<>(positions);
                                newPositions.add(position);
                                boolean match = match(board, xlen, ylen, nextNode, lastHeadPosition, position, newPositions, results);
                                if (match) {
                                    result = true;
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }


    private int[] findPosition(char[][] board, int xLength, int yLength, char c, int[] lastHeadPosition) {
        int i_start = lastHeadPosition == null ? 0 : lastHeadPosition[1];
        int j_start = lastHeadPosition == null ? 0 : lastHeadPosition[0] + 1;
        for (int i = i_start; i < yLength; i++) {
            char[] xArray = board[i];
            int j;
            if (i == i_start) {
                j = j_start;
                if (j >= xLength) continue;
            } else {
                j = 0;
            }
            for (; j < xLength; j++) {
                if (xArray[j] == c) {
                    return new int[]{j, i};
                }
            }
        }
        return null;
    }

    private boolean contains(LinkedList<int[]> positions, int[] position) {
        for (int[] ints : positions) {
            if (Arrays.equals(ints, position)) return true;
        }
        return false;
    }


    public static class Trie {

        private Node root = new Node();

        public Trie() {
        }

        public void insert(String word) {
            Node node = root;
            int index = 0;
            while (index < word.length()) {
                char c = word.charAt(index);
                index++;
                if (node.contains(c)) {
                    node = node.get(c);
                } else {
                    Node next = new Node();
                    next.val = c;
                    node.put(c, next);
                    node = next;
                }
            }
            node.setEnd(word);
        }

        public boolean search(String word) {
            Node prefix = getPrefix(word);
            return prefix != null && prefix.isEnd();
        }

        public boolean startsWith(String prefix) {
            return getPrefix(prefix) != null;
        }

        private Node getPrefix(String prefix) {
            Node node = root;
            int index = 0;
            while (node != null && index < prefix.length()) {
                char c = prefix.charAt(index);
                index++;
                node = node.get(c);
            }
            return node;
        }
    }

    public static class Node {

        char val;

        Node[] links;

        boolean isEnd;

        String word;

        List<Character> linkValues = new ArrayList<>();

        public Node() {
            links = new Node[26];
        }

        public boolean contains(char c) {
            return links[c - 'a'] != null;
        }

        public void put(char c, Node node) {
            int index = c - 'a';
            links[index] = node;
            linkValues.add(c);
        }

        public Node get(char c) {
            return links[c - 'a'];
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(String word) {
            this.isEnd = true;
            this.word = word;
        }
    }
}