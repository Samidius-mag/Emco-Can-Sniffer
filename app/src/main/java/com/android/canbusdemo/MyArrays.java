package com.android.canbusdemo;

/**
 * Created by zyz 2020/04/14
 * Arrays add/delete/modify/check
 */

public class MyArrays {

    private int[] arr;
    public MyArrays() {
        arr = new int[0];
    }

    //Length
    public int getLength(){
        return arr.length;
    }

    //Create
    public int[] getArrays() {
        return arr;
    }

    //Add
    public void add(int num) {
        int[] arrays=new int[arr.length+1];
        //copy
        System.arraycopy(arr, 0, arrays, 0, arr.length);
        arrays[arr.length]=num;
        arr=arrays;
    }

    public void add_mask(int num) {
        int[] arrays=new int[arr.length+1];
        //copy
        System.arraycopy(arr, 0, arrays, 0, arr.length);
        arrays[arr.length]=num;
        arr=arrays;
    }

    public void clear(){
        arr = new int[0];
    }

    //Delete
    public void delete(int index) {
        if(arr.length==0) {
            System.out.println("Array is null !!!");
        }else {
            int[] arrays=new int[arr.length-1];
            System.arraycopy(arr, 0, arrays, 0, index);//复制目标元素之前的东西
            System.arraycopy(arr, index+1, arrays, index, arrays.length-index);//复制目标元素之后的元素
            arr=arrays;
        }
    }

    //Update
    public void update(int index,int num) {
        if(arr.length==0) {
            System.out.println("Array is null !!!");
        }else {
            arr[index]=num;
        }
    }

    //Checkout
    public int quary(int index) {
        if(arr.length==0) {
            System.out.println("Array is null !!!");
            return 0;
        }
        return arr[index];
    }
}