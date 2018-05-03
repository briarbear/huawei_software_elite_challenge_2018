package com.elasticcloudservice.predict;



public class Predict {



	/**
	 * 预测函数
	 * @param ecsContent 训练数据 字符串数组，每一行内容对应于源数据内容，如：56498c50-84e4   flavor15	2015-01-01 19:03:32
	 * @param inputContent 输入参数
	 * @return
	 */
	public static String[] predictVm(String[] ecsContent, String[] inputContent) {

		/** =========do your work here========== **/

		//预测虚拟机数量，以字符串数组返回，
		String[] results = PredictFlavor.predict(ecsContent,inputContent);


		return results;

	}


}
