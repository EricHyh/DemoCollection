package com.hyh.arithmetic.trie;

/**
 * @author Administrator
 * @description
 * @data 2020/6/9
 */
public class Trie {

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
        node.setEnd();
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

    public static class Node {

        char val;

        Node[] links;

        boolean isEnd;

        public Node() {
            links = new Node[26];
        }

        public boolean contains(char c) {
            return links[c - 'a'] != null;
        }

        public void put(char c, Node node) {
            links[c - 'a'] = node;
        }

        public Node get(char c) {
            return links[c - 'a'];
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd() {
            isEnd = true;
        }
    }
}