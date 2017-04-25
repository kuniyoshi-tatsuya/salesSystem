package jp.alhinc.kuniyoshi_tatsuya.calculate_sales;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSalesSystem {

	public static HashMap<String,String> readBranchData(File file) throws IOException{
		HashMap<String,String> branch = new HashMap<String,String>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(file));
			String str;
			while((str = br.readLine()) != null){
				String[] split = str.split(",");
				if(split.length != 2){
					return new HashMap<String,String>();
				}
				branch.put(split[0],split[1]);
			}
		}finally{
			 br.close();
		}
		return branch;

	}

	public static HashMap<String,String> readCommodityData(File file) throws IOException{
		HashMap<String,String> commodity = new HashMap<String,String>();
		BufferedReader br = null;
		try{
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String str;
			while((str = br.readLine()) != null){
				String[] split = str.split(",");
				if(split.length != 2){
					return new HashMap<String,String>();
				}
				commodity.put(split[0],split[1]);
			}

		}finally{
			br.close();
		}
		return commodity;
	}

	public static String[][] readSalesData(ArrayList<File> rcdfiles, Map<String, String> branch, Map<String, String> commodity, String arg) throws Exception{

		String[][] salesdata = new String[rcdfiles.size()][3];
		final int BRANCH_CODE = 0, COMMODITY_CODE = 1;
		BufferedReader br = null;
		try{
			for(int i=0; i<rcdfiles.size(); i++){
				br = new BufferedReader(new FileReader(new File(arg, rcdfiles.get(i).getName())));
				String indata;
				int cnt=0;

				while((indata = br.readLine()) != null){
					if(cnt == 3){
						String[][] error = new String[1][2];
						error[0][0] = rcdfiles.get(i).getName();
						error[0][1] = "2";
						return error;
					}
					salesdata[i][cnt] = indata;
					cnt++;
				}
				if(cnt != 3){
					String[][] error = new String[1][2];
					error[0][0] = rcdfiles.get(i).getName();
					error[0][1] = "2";
					return error;
				}

				if(!branch.containsKey(salesdata[i][BRANCH_CODE])){
					String[][] error = new String[1][2];
					error[0][0] = rcdfiles.get(i).getName();
					error[0][1] = "0";
					return error;
				}
				if(!commodity.containsKey(salesdata[i][COMMODITY_CODE])){
					String[][] error = new String[1][2];
					error[0][0] = rcdfiles.get(i).getName();
					error[0][1] = "1";
					return error;
				}
			}
		}catch(IOException e){
			throw e;
		}finally {
			br.close();
		}

		return salesdata;
	}

	public static ArrayList<File> checkSalesData(File directory){

		ArrayList<File> files = new ArrayList<>();
		for(File fl : directory.listFiles()){
			if(fl.getName().endsWith(".rcd") && fl.getName().matches("^.{12}$") && fl.isFile()) {
				files.add(fl);
			}
		}

		long min = Long.parseLong(files.get(0).getName().substring(0,8));
		long max = Long.parseLong(files.get(files.size()-1).getName().substring(0,8));
		if(files.size() == max-min+1)return files;
		else return new ArrayList<File>();
	}

	public static Map<String,Long> initializeMap(HashMap<String,String> source){
		HashMap<String,Long> map = new HashMap<>();
		for(Map.Entry<String, String> e : source.entrySet()){ //支店の数だけ回す
			map.put(e.getKey(), 0L);
		}

		return map;
	}

	public static Map<String,Long> fileOutputBranch(Map<String,String> map1, Map<String,Long> map2, File file) throws Exception{
		List<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String,Long>>(map2.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {
			public int compare(
				Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});

		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(file));
			String str = "0";
			for (Entry<String,Long> e : entries) {
				str = e.getKey() + "," + map1.get(e.getKey()) + "," + e.getValue();
	            bw.write(str);
	            bw.newLine();
	        }
		}catch(FileNotFoundException e){
			throw e;
		}catch(Exception e){
			System.out.println(e);
		}finally{
			bw.close();
		}
		return map2;
	}

	public static Map<String,Long> fileOutputCommodity(Map<String,String> map1, Map<String,Long> map2, File file) throws Exception{

		List<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String,Long>>(map2.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {
			public int compare(
				Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});

		BufferedWriter bw = null;
		try{

			bw = new BufferedWriter(new FileWriter(file));
			String str = "0";
			for (Entry<String,Long> e : entries) {
				str = e.getKey() + "," + map1.get(e.getKey()) + "," + e.getValue();
	            bw.write(str);
	            bw.newLine();
	        }
		}catch(FileNotFoundException e){
			throw e;
		}catch(Exception e){
			System.out.println(e);
		}finally{
			bw.close();
		}

		return map2;

	}

	public static void main(String[] args) throws Exception{
		try {

			if(args.length != 1){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}

			if(!new File(args[0], "branch.lst").exists()){
				System.out.println("支店定義ファイルが存在しません");
				return ;
			}
			HashMap<String,String> branch = readBranchData(new File(args[0], "branch.lst")); //支店コードファイル読み込み
			if(branch.size() == 0){
				System.out.println("支店定義ファイルのフォーマットが不正です");
				return;
			}
			for(Map.Entry<String, String> e : branch.entrySet()) {
				if(!e.getKey().matches("^\\d{3}$")){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
			}

			if(!new File(args[0], "commodity.lst").exists() ){
				System.out.println("商品定義ファイルが存在しません");
				return ;
			}
			HashMap<String,String> commodity = readCommodityData(new File(args[0], "commodity.lst")); //商品定義ファイル読み込み
			if(commodity.size() == 0){
				System.out.println("商品定義ファイルのフォーマットが不正です");
				return;
			}
			for(Map.Entry<String, String> e : commodity.entrySet()) {
				if(!e.getKey().matches("^\\w{8}$") || commodity.size() == 0){
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
			}

			Map<String,Long> branchsales = new HashMap<>(); //支店別売上集計データ
			Map<String,Long> commoditysales = new HashMap<>(); //商品別売上集計データ
			final int BRANCH_CODE = 0, COMMODITY_CODE = 1, SALES = 2;

			ArrayList<File> rcdfiles = checkSalesData(new File(args[0])); //.rcdファイルを取得する
			if(rcdfiles.size() == 0){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}

			String[][] salesdata = new String[rcdfiles.size()][3]; //売上データ
			salesdata = readSalesData(rcdfiles, branch, commodity,args[0]);//売上ファイル読み込み
			if(salesdata[0][1] == "2"){
				System.out.println(salesdata[0][0] + "フォーマットが不正です");
				return;
			}
			if(salesdata[0][1] == "0"){
				System.out.println(salesdata[0][0] + "の支店コードが不正です");
				return;
			}
			if(salesdata[0][1] == "1"){
				System.out.println(salesdata[0][0] + "の商品コードが不正です");
				return;
			}

			branchsales = initializeMap(branch);
			commoditysales = initializeMap(commodity);

			for(String[] salesFile : salesdata){
				long n = Long.parseLong(salesFile[SALES]);
				long m = branchsales.get(salesFile[BRANCH_CODE]);
				branchsales.put(salesFile[BRANCH_CODE], n+m);
				if(!String.valueOf(n+m).matches("^\\d{1,10}$")){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			}

			for(String[] salesFile : salesdata){
				long n = Long.parseLong(salesFile[SALES]);
				long m = commoditysales.get(salesFile[COMMODITY_CODE]);
				commoditysales.put(salesFile[COMMODITY_CODE], n+m);
				if(!String.valueOf(n+m).matches("^\\d{1,10}$")){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			}

			branchsales = fileOutputBranch(branch, branchsales, new File(args[0], "branch.out")); //branchをアウトプット
			commoditysales = fileOutputCommodity(commodity, commoditysales, new File(args[0], "commodity.out")); //commodityをアウトプット

		}catch (Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
	}
}