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
public class Solution {

    /**
     * words = ["oath","pea","eat","rain"] and board =
     * [
     * ['o','a','a','n'],
     * ['e','t','a','e'],
     * ['i','h','k','r'],
     * ['i','f','l','v']
     * ]
     * <p>
     * 输出: ["eat","oath"]
     */
    public List<String> findWords(char[][] board, String[] words) {
        List<String> results = new ArrayList<>();

        int xLength = board[0].length;
        int yLength = board.length;

        for (String word : words) {
            LinkedList<int[]> match = match(board, xLength, yLength, null, null, word, 0, null);
            if (match != null && match.size() == word.length()) {
                results.add(word);
            }
        }
        return results;
    }

    /**
     * [["a","a"]]
     * ["aaa"]
     */
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

    private LinkedList<int[]> match(char[][] board,
                                    int xLength,
                                    int yLength,
                                    int[] lastHeadPosition,
                                    int[] lastPosition,
                                    String word,
                                    int index,
                                    LinkedList<int[]> positions) {
        if (index == word.length()) return positions;
        char charAt = word.charAt(index);
        if (lastPosition == null) {
            int[] position = findPosition(board, xLength, yLength, charAt, lastHeadPosition);
            if (position == null) return null;
            positions = new LinkedList<>();
            positions.add(position);

            LinkedList<int[]> match = match(board, xLength, yLength, lastHeadPosition, position, word, index + 1, positions);
            if (match != null) {
                return match;
            } else {
                return match(board, xLength, yLength, position, null, word, 0, null);
            }
        } else {
            int lastX = lastPosition[0];
            int lastY = lastPosition[1];

            int lx = lastX - 1;
            int ly = lastY;
            if (lx >= 0 && board[ly][lx] == charAt) {
                int[] position = new int[]{lx, ly};
                if (!contains(positions, position)) {
                    LinkedList<int[]> newPositions = new LinkedList<>(positions);
                    newPositions.add(position);
                    LinkedList<int[]> match = match(board, xLength, yLength, lastHeadPosition, position, word, index + 1, newPositions);
                    if (match != null) {
                        return match;
                    }
                }
            }

            int tx = lastX;
            int ty = lastY - 1;
            if (ty >= 0 && board[ty][tx] == charAt) {
                int[] position = new int[]{tx, ty};
                if (!contains(positions, position)) {
                    LinkedList<int[]> newPositions = new LinkedList<>(positions);
                    newPositions.add(position);
                    LinkedList<int[]> match = match(board, xLength, yLength, lastHeadPosition, position, word, index + 1, newPositions);
                    if (match != null) {
                        return match;
                    }
                }
            }

            int rx = lastX + 1;
            int ry = lastY;
            if (rx < xLength && board[ry][rx] == charAt) {
                int[] position = new int[]{rx, ry};
                if (!contains(positions, position)) {
                    LinkedList<int[]> newPositions = new LinkedList<>(positions);
                    newPositions.add(position);
                    LinkedList<int[]> match = match(board, xLength, yLength, lastHeadPosition, position, word, index + 1, newPositions);
                    if (match != null) {
                        return match;
                    }
                }
            }

            int bx = lastX;
            int by = lastY + 1;
            if (by < yLength && board[by][bx] == charAt) {
                int[] position = new int[]{bx, by};
                if (!contains(positions, position)) {
                    LinkedList<int[]> newPositions = new LinkedList<>(positions);
                    newPositions.add(position);
                    LinkedList<int[]> match = match(board, xLength, yLength, lastHeadPosition, position, word, index + 1, newPositions);
                    if (match != null) {
                        return match;
                    }
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
}