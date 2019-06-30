package com.sahil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JavaConcurrency {
	
	public static void main(String args[]){
		long start = System.nanoTime();
		double netAsset = netAssetConcurrent(readStock());
		//double netAsset = netAsset(readStock());
		System.out.println("your net asset is ...."+ netAsset);
		System.out.print("Total time...."+(System.nanoTime()-start)/1000000+"msec");
	}

	public static Map<String,Integer> readStock(){
		BufferedReader bufferedReader = null;
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			bufferedReader = new BufferedReader(new FileReader("stock.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String stockInfo;
		try {
			while((stockInfo = bufferedReader.readLine()) != null){
				String[] str = stockInfo.split(",");
				map.put(str[0], Integer.parseInt(str[1]));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	public static double netAsset(Map<String,Integer> map){
		double netAsset = 0.0;
		YahooFinance yahooFinance = new YahooFinance();
		for(String stockName:map.keySet()){
			netAsset = netAsset + (yahooFinance.getPrice(stockName)*map.get(stockName));
		}
		return netAsset;
	}
	
	public static double netAssetConcurrent(final Map<String,Integer> map){
		double netAsset = 0.0;
		final YahooFinance yahooFinance = new YahooFinance();
		List<Callable<Double>> list = new ArrayList();
		int core = Runtime.getRuntime().availableProcessors();
		float bc = 0.9f;
		
		int poolSize = (int)(core/(1-bc));
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		for(final String stockName:map.keySet()){
			list.add(new Callable<Double>() {
				public Double call() throws Exception {
					return yahooFinance.getPrice(stockName)*map.get(stockName);
				}
			});
		}
		try {
			List<Future<Double>> stockList = executorService.invokeAll(list);
			for(Future<Double> future : stockList ){
				try {
					netAsset = netAsset + future.get();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			executorService.shutdownNow();
		}
		
		return netAsset;
	}
	
}

//third party api
class YahooFinance{
	
	
	public double getPrice(String stockName){
		System.out.println("getting price of stock.."+stockName);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 350.00;
	}
	
}