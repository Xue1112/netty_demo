package com.itheima;


import java.util.Arrays;
import java.util.Random;

public class App {

    public static void main(String[] args) {

        int[] nums = new int[]{1,3,2,6,7,8,9};
        App app = new App();
        int[] sort = app.sort(nums, 2);
        System.out.println(Arrays.toString(nums));
        System.out.println(Arrays.toString(sort));
    }

    public int[]  sort(int[] nums,int op) {
        int[] res;
        switch (op) {
            case 1 : // 普通排序
                res = quickSort(nums);
                break;
            case 2 : // 特殊排序
                res = special(nums);
                break;
            default: res = quickSort(nums);
        }
        return res;
    }

    public int[] special(int[] nums) {
        // 先用快排排好
        nums = quickSort(nums);
        // 用空间换时间 , 按照要求组好数据
        int[] res = new int[nums.length];
        int l = 0;
        int r = nums.length-1;
        int i = 0;
        while (l<=r) {
            if ((i & 1) == 1) {
                res[r--] = nums[i++];
            }else {
                res[l++] = nums[i++];
            }
        }
        return res;
    }

    // 快排
    public int[] quickSort(int[] nums) {
        randomizedQuicksort(nums, 0, nums.length - 1);
        return nums;
    }

    public void randomizedQuicksort(int[] nums, int l, int r) {
        if (l < r) {
            int pos = randomizedPartition(nums, l, r);
            randomizedQuicksort(nums, l, pos - 1);
            randomizedQuicksort(nums, pos + 1, r);
        }
    }

    public int randomizedPartition(int[] nums, int l, int r) {
        int i = new Random().nextInt(r - l + 1) + l;
        swap(nums, r, i);
        return partition(nums, l, r);
    }

    public int partition(int[] nums, int l, int r) {
        int pivot = nums[r];
        int i = l - 1;
        for (int j = l; j <= r - 1; ++j) {
            if (nums[j] <= pivot) {
                i = i + 1;
                swap(nums, i, j);
            }
        }
        swap(nums, i + 1, r);
        return i + 1;
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}
