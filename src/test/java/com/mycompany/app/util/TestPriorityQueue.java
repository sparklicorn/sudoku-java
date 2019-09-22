package com.mycompany.app.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;


public class TestPriorityQueue {

    @Test
    public void testConstructor() {

        PriorityQueue<Integer> pq = new PriorityQueue<>((Integer a, Integer b) -> {
            return Integer.compare(a, b);
        });

    }

    @Test
    public void testOffer() {
        Comparator<Integer> comparator = (Integer a, Integer b) -> {
            return Integer.compare(a, b);
        };

        ArrayList<Integer> list = new ArrayList<>();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int n = 1000;
        int max = 1 << 20;
        for (int x = 0; x < n; x++) {
            list.add(rand.nextInt(max) * 2 - (max));
        }

        //Put all numbers into PriorityQueue.
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);

        for (int x : list) {
            pq.offer(x);
        }

    }

    @Test
    public void testPoll() {
        Comparator<Integer> comparator = (Integer a, Integer b) -> {
            return Integer.compare(a, b);
        };

        ArrayList<Integer> list = new ArrayList<>();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int n = 1000;
        int max = 1 << 20;
        for (int x = 0; x < n; x++) {
            list.add(rand.nextInt(max) * 2 - (max));
        }
        ArrayList<Integer> sortedList = new ArrayList<>(list);
        sortedList.sort(comparator);

        //Put all numbers into PriorityQueue.
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);

        for (int x : list) {
            pq.offer(x);
        }

        for (int i = 0; i < n; i++) {
            assertEquals(sortedList.get(i), pq.poll());
        }

    }


}