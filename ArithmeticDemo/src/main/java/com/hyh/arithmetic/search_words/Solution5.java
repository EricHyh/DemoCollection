package com.hyh.arithmetic.search_words;

import java.util.ArrayList;
import java.util.List;

public class Solution5 {

    public List<String> findWords(char[][] board, String[] words) {
        List<String> results = new ArrayList<>();
        Trie trie = new Trie();
        for (String word : words) {
            trie.insert(word);
        }
        Node root = trie.root;
        int xlen = board[0].length;
        int ylen = board.length;

        for (int row = 0; row < ylen; row++) {
            for (int col = 0; col < xlen; col++) {
                if (root.contains(board[row][col])) {
                    backtracking(board, row, col, root, results);
                }
            }
        }
        return results;
    }

    public void backtracking(char[][] board, int row, int col, Node parent, List<String> results) {
        char c = board[row][col];
        Node currNode = parent.get(c);
        if (currNode.isEnd()) {
            results.add(currNode.word);
            currNode.clearEnd();
        }

        int[] rowOffset = new int[]{0, -1, 0, 1};
        int[] colOffset = new int[]{-1, 0, 1, 0};

        board[row][col] = '#';

        for (int i = 0; i < 4; i++) {
            int newRow = row + rowOffset[i];
            int newCol = col + colOffset[i];
            if (newRow < 0 || newCol >= board.length || newCol < 0 || newCol >= board[0].length) {
                continue;
            }
            if (currNode.contains(board[newRow][newCol])) {
                backtracking(board, newRow, newCol, currNode, results);
            }
        }

        board[row][col] = c;

        if (currNode.linkValues.isEmpty()) {
            parent.deleteNode(c);
        }
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
            int index = c - 'a';
            if (index < 0 || index >= 26) return false;
            return links[index] != null;
        }

        public void put(char c, Node node) {
            int index = c - 'a';
            links[index] = node;
            linkValues.add(c);
        }

        public Node get(char c) {
            int index = c - 'a';
            if (index < 0 || index >= 26) return null;
            return links[index];
        }


        public void deleteNode(char c) {
            int index = c - 'a';
            if (index < 0 || index >= 26) return;
            links[index] = null;
            linkValues.remove((Character) c);
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(String word) {
            this.isEnd = true;
            this.word = word;
        }

        public void clearEnd() {
            this.isEnd = false;
            this.word = null;
        }
    }


}
