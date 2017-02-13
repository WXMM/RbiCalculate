package com.rbi;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Calculate {
	
	
 static String    sql  = null;  
 static DBHelper  db1  = null;  
 static ResultSet ret  = null; 
 
 public static final String url = "jdbc:mysql://127.0.0.1/rbi_bs?useUnicode=true&characterEncoding=utf8";  
 public static final String name = "com.mysql.jdbc.Driver";  
 public static final String user = "root";  
 public static final String password = "";  

 public Connection        conn        = null;  
 public PreparedStatement pst         = null;	
 public Statement         stmt        = null;
 
 public String[] riskRate(int PlantId){
	 
	 String[] re = new String[5];    //返回数组
	 
	 Calculate calculate = new Calculate();
	 List<Map<String,Object>> plantInfo                  = calculate.getPlantInfo(PlantId);
     List<Map<String,Object>> wallboardInfo              = calculate.getWallboardInfo(PlantId);
     List<Map<String,Object>> plantMech                  = calculate.getPlantMech(PlantId);
     List<Map<String,Object>> plantTest                  = calculate.getPlantTest(PlantId);
     List<Map<String,Object>> wallPlantCorrosion         = calculate.getWallPlantCorrosion(PlantId);
     List<Map<String,Object>> bottomEdgePlantCorrosion   = calculate.getBottomPlantCorrosion(PlantId,2,1);
     List<Map<String,Object>> bottomMiddlePlantCorrosion = calculate.getBottomPlantCorrosion(PlantId,2,2);
     
     if(wallboardInfo.isEmpty()){
         re[0] = "该设备没有添加壁板，请前往基本信息管理添加壁板";
         re[1] = ""+0;
         return re;
     }
     if(plantMech.isEmpty()){
    	 re[0] = "该设备没有进行损伤机理筛选，请先进行损伤机理筛选";
    	 re[1] = ""+0;
    	 return re;
     }
     
     double useDate = 0;
     useDate  =  calculate.getUseDate(plantInfo.get(0).get("useDate").toString());
	 
//风险矩阵图 1 代表失效可能性等级  2代表失效后果等级
	 String[][] riskMatrixTable = new String[6][6];
	 riskMatrixTable[1][1]="低风险";
     riskMatrixTable[1][2]="低风险";
     riskMatrixTable[1][3]="中风险";
     riskMatrixTable[1][4]="中风险";
     riskMatrixTable[1][5]="中高风险";
     riskMatrixTable[2][1]="低风险";
     riskMatrixTable[2][2]="低风险";
     riskMatrixTable[2][3]="中风险";
     riskMatrixTable[2][4]="中风险";
     riskMatrixTable[2][5]="中高风险";
     riskMatrixTable[3][1]="低风险";
     riskMatrixTable[3][2]="低风险";
     riskMatrixTable[3][3]="中风险";
     riskMatrixTable[3][4]="中高风险";
     riskMatrixTable[3][5]="高风险";
     riskMatrixTable[4][1]="中风险";
     riskMatrixTable[4][2]="中风险";
     riskMatrixTable[4][3]="中高风险";
     riskMatrixTable[4][4]="中高风险";
     riskMatrixTable[4][5]="高风险";
     riskMatrixTable[5][1]="中高风险";
     riskMatrixTable[5][2]="中高风险";
     riskMatrixTable[5][3]="中高风险";
     riskMatrixTable[5][4]="高风险";
     riskMatrixTable[5][5]="高风险"; 
//-----------------------------------------计算当前风险---------------------------------------

// ------------------------------------------壁板风险----------------------------------------
    
     
     double  maxWallDamageFactor    = 0;
     double  maxWallFailConsequence = 0;
     String  maxValueLayerId        = "";
     
     List<Map<String, Object>> wallFactor = new ArrayList<Map<String, Object>>(); 
      
     for (Map<String, Object> WallPlantCorrosion : wallPlantCorrosion)  
     {  
    	 Map<String, Object>    wallFactorMap = new HashMap<String, Object>();
    	 wallFactorMap.put("layerNO", WallPlantCorrosion.get("layerNO").toString());
    	 wallFactorMap.put("layerId", WallPlantCorrosion.get("layerId").toString());
    	 wallFactorMap.put("thicknessType", WallPlantCorrosion.get("thicknessType").toString());
    	 wallFactorMap.put("corrosionSpeed", Double.parseDouble(WallPlantCorrosion.get("long_termCorrosion").toString()));
    	 
//-------------------------------------计算壁板损伤因子----------------------------------------
//       1、壁板板减薄次因子
    	 double wallReductionDamageFactor = calculate.getReductionDamageFactor(
                 
                     1,                                                             //代表壁板
                     Double.parseDouble(WallPlantCorrosion.get("long_termCorrosion").toString()),            //该层壁板腐蚀速率
                     useDate,                                                                                    //使用时间(储罐使用时间)
                     Double.parseDouble(WallPlantCorrosion.get("namelyThickness").toString()),               //壁板初始厚度(目前是名义厚度)

                     Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                      //检验次数
                     plantTest.get(0).get("wallCheckValidity").toString(),                                    //检验有效性
                     
                     plantInfo.get(0).get("wallboardLinkType").toString(),                                    //壁板连接形式
                     Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //是否按规定维护
                     Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //基础沉降评价
                
             );
    	 wallFactorMap.put("wallReductionDamageFactor", wallReductionDamageFactor);
    	 
//       2、壁板外部损伤因子
          double wallOutDamageFactor  = calculate.getOutDamageFactor(
        		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //操作温度
        		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //设计温度
                  1,                                                                                            //代表壁板
                  Double.parseDouble(WallPlantCorrosion.get("namelyThickness").toString()),                    //壁板初始厚度(目前是名义厚度)

                  Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //管道复杂度
                  Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //是否保温层含氯
                  Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //是否管支架补偿
                  Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //是否界面补偿
                  Integer.parseInt(plantMech.get(0).get("wallOutDamageMechanismId").toString()),                //外部损伤机理
                 
                  useDate,                                                                                        //使用时间(储罐使用时间)
                  Integer.parseInt(plantInfo.get(0).get("isWallboardKeepWarm").toString()),                                //是否有保温层
                  Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),                            //保温层质量
                  Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                             //涂层质量
                  useDate, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),                         //涂层使用时间
                  plantInfo.get(0).get("wallboardLinkType").toString(),                                       //连接形式
                  
                 Integer.parseInt(plantMech.get(0).get("wallSCCMechanismId").toString()),                   //SCC损伤机理
                 Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                            //检验次数
                 plantTest.get(0).get("wallCheckValidity").toString(),                                           //检验有效性

                 Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //设备环境
                 Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //是否按规定维护
                 Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //基础沉降评价
                 Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //是否介质含水
                 
                 Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //水中的H2S值
                 Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //水中PH值
                 Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //是否焊后热处理
                 Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //最大布氏硬度
                 Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //水中cl离子含量

                 Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //是否伴热
                 Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOH浓度
                 Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //是否蒸汽吹扫
                 Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //是否进行应力消除
                 Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //热历史
                 Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //是否有停机保护
                 plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //环境含有物
                 Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //钢板中硫含量
                 Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //碳酸盐浓度
         );
          wallFactorMap.put("wallOutDamageFactor", wallOutDamageFactor);
    	 
//     3、壁板SCC损伤因子

          double wallSCCDamageFactor = calculate.getSCCDamageFactor(
        		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //操作温度
        		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //设计温度
        		  Integer.parseInt(plantMech.get(0).get("wallSCCMechanismId").toString()),            //SCC损伤机理
        		  Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                 //检验次数
                  plantTest.get(0).get("wallCheckValidity").toString(),                               //检验有效性
                  0,                                                                                  //计算外部损伤因子的时候不需要计算敏感性高低，直接输入严重程度指数
                 
                  Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //是否介质含水
                  Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //水中的硫化氢含量
                  Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //水中pH值
                  Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //是否焊后热处理
                  Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //最大布氏硬度
                  Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //水中的氯离子含量

                  Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //是否伴热
        		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOH浓度
        		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //是否蒸汽吹扫
        		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //是否进行应力消除
        		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //热历史
        		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //是否停机保护
        		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //环境含有物
        		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //钢板中硫含量
        		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //碳酸盐浓度
              );
              wallFactorMap.put("wallSCCDamageFactor", wallSCCDamageFactor);
             
//            4、壁板脆性断裂因子
              wallFactorMap.put("wallBrittleDamageFactor", 0);
           
            //------------------------------------计算损伤因子-----------------------------------------------------------

//            计算得到损伤因子的值
       double  wallDamageFactor = calculate.getDamageFaction(
                0,                                     //腐蚀类型，壁板为均匀腐蚀，底板为局部腐蚀 0代表均匀腐蚀 1代表局部腐蚀
                wallReductionDamageFactor,     //减薄损伤因子
                wallOutDamageFactor,           //外部损伤因子
                wallSCCDamageFactor,           //应力腐蚀开裂损伤因子
                0             //脆性断裂损伤因子
            );
       wallFactorMap.put("wallDamageFactor", wallDamageFactor);
               
//             壁板失效后果
       double wallFailConsequence = calculate.getFailureWallConsequence(
    		   Double.parseDouble(plantInfo.get(0).get("D").toString()),                            //罐直径
    		   Double.parseDouble(WallPlantCorrosion.get("height").toString()),         //储罐单层壁板高度
    		   Integer.parseInt(WallPlantCorrosion.get("layerNO").toString()),           //第几层壁板
               Integer.parseInt(plantInfo.get(0).get("sensitiveEnvironment").toString()),             //失效后果可接受的基准 单位：万元
    		   Double.parseDouble(plantInfo.get(0).get("fillH").toString()),                         //泄露孔上方的液体高度
    		   Double.parseDouble(plantInfo.get(0).get("fMatCost_").toString()),                     //材料价格系数
    		   Double.parseDouble(plantInfo.get(0).get("stopLoss").toString()),                      //停产造成的损失
    		   Double.parseDouble(plantInfo.get(0).get("overflowPercentage").toString()),            //溢出围堰的流体百分比
    		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageIn").toString()),          //溢出围堪但仍在罐区内，地表土壤中的流体百分比
    		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageOut").toString())          //溢出围堪且已流到罐区外，地表土壤中的流体百分比
           ); 
       wallFactorMap.put("wallFailConsequence", wallFailConsequence);
       wallFactor.add(wallFactorMap);
    	 
    	 //获取损伤因子最大的值测点和值
    	 if(wallDamageFactor > maxWallDamageFactor){
    		 maxWallDamageFactor    = wallDamageFactor;
    		 maxValueLayerId        = WallPlantCorrosion.get("layerId").toString();
    		 maxWallFailConsequence = wallFailConsequence;
    	 } else {
    		 maxWallDamageFactor    = wallDamageFactor;
    		 maxValueLayerId        = WallPlantCorrosion.get("layerId").toString();
    		 maxWallFailConsequence = wallFailConsequence;
    	 }
     } 
    
//-----------------------------壁板损伤因子---------------------------------------------------------
//   找出壁板板最大损伤因子，按照最大损伤因子计算风险等级     
  double fAcceptBaseQ_             =    Double.parseDouble(plantInfo.get(0).get("failConseqenceAccept").toString());        //失效后果可接受基准
  String wallFailConsequenceLevel  =    "A";
  int    wallFailConsequenceLevelIndex = 1;
  if(maxWallFailConsequence < fAcceptBaseQ_){
	  wallFailConsequenceLevel = "A";
	  wallFailConsequenceLevelIndex = 1;
  } else if(maxWallFailConsequence > fAcceptBaseQ_ && maxWallFailConsequence <= fAcceptBaseQ_*10){
	  wallFailConsequenceLevel = "B";
	  wallFailConsequenceLevelIndex = 2;
  } else if(maxWallFailConsequence > fAcceptBaseQ_*10 && maxWallFailConsequence <= fAcceptBaseQ_*100){
	  wallFailConsequenceLevel = "C";
	  wallFailConsequenceLevelIndex = 3;
  } else if(maxWallFailConsequence > fAcceptBaseQ_*100 && maxWallFailConsequence <= fAcceptBaseQ_*1000){
	  wallFailConsequenceLevel = "D";
	  wallFailConsequenceLevelIndex = 4;
  } else if(maxWallFailConsequence > fAcceptBaseQ_*1000){
	  wallFailConsequenceLevel = "E";
	  wallFailConsequenceLevelIndex = 5;
  }
  
//壁板失效可能性
 double wallAverageFailurePro = calculate.getAverageFailurePro(0,  Float.parseFloat(plantInfo.get(0).get("breakSize").toString()));          //壁板平均失效可能性
 double wallFailurePro        = calculate.getFailurePro(1, wallAverageFailurePro, maxWallDamageFactor);  //壁板失效可能性
 int    wallFailureProLevel   = 1;
//计算壁板失效可能性等级
 if(wallFailurePro > 0 && wallFailurePro <= 0.00001 ){
	 wallFailureProLevel = 1;
 }else if(wallFailurePro > 0.00001 && wallFailurePro <= 0.00010){
	 wallFailureProLevel = 2;
 }else if(wallFailurePro > 0.00010 && wallFailurePro <= 0.00100){
	 wallFailureProLevel = 3;
 }else if(wallFailurePro > 0.00100 && wallFailurePro <= 0.01000){
	 wallFailureProLevel = 4;
 }else if(wallFailurePro > 0.01000){
	 wallFailureProLevel = 5;
 }

 
//-----------------------------------------计算底板风险等级--------------------------------------

 double wallRisk      = maxWallFailConsequence * wallFailurePro;                              //壁板风险
 String wallRiskLevel = riskMatrixTable[wallFailureProLevel][wallFailConsequenceLevelIndex];  //壁板风险等级

 
//-------------------------底板风险计算分割线-----------------------------------------------------
 
 
 double  maxBottomEdgeDamageFactor    = 0;
 double  maxBottomEdgeFailConsequence = 0;
 double  maxBottomEdgeCorrosion       = 0;
 String  maxBottomEdgeValueLayerId        = "";
 
 List<Map<String, Object>> bottomEdgeFactor = new ArrayList<Map<String, Object>>(); 
 
 for (Map<String, Object>  bottomEdgeCorrosion : bottomEdgePlantCorrosion)  
 {  
	 Map<String, Object>       bottomEdgeFactorMap = new HashMap<String, Object>(); 
//-------------------------------------边缘板风险----------------------------------------
//   1、边缘板减薄次因子
	 double bottomEdgeReductionDamageFactor = calculate.getReductionDamageFactor(
             
                 1,                                                                                       //代表壁板
                 Double.parseDouble(bottomEdgeCorrosion.get("long_termCorrosion").toString()),            //该层壁板腐蚀速率
                 useDate,                                                                                     //使用时间(储罐使用时间)
                 Double.parseDouble(plantInfo.get(0).get("bottomEdgeNamelyThickness").toString()),               //壁板初始厚度(目前是名义厚度)

                 Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                      //检验次数
                 plantTest.get(0).get("bottomCheckValidity").toString(),                                    //检验有效性
                 
                 plantInfo.get(0).get("bottomLinkType").toString(),                                    //壁板连接形式
                 Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //是否按规定维护
                 Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //基础沉降评价
            
         );
	 bottomEdgeFactorMap.put("bottomEdgeReductionDamageFactor", bottomEdgeReductionDamageFactor);
	 
//   2、边缘板外部损伤因子
      double bottomEdgeOutDamageFactor  = calculate.getOutDamageFactor(
    		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //操作温度
    		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //设计温度
              1,                       //代表壁板
              Double.parseDouble(plantInfo.get(0).get("bottomEdgeNamelyThickness").toString()),                    //壁板初始厚度(目前是名义厚度)

              Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //管道复杂度
              Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //是否保温层含氯
              Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //是否管支架补偿
              Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //是否界面补偿
              Integer.parseInt(plantMech.get(0).get("floorOutDamageMechanismId").toString()),                //外部损伤机理
             
              useDate,                                                                                        //使用时间(储罐使用时间)
              0,                                                                                          //是否有保温层（底板没有保温层）
              Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),               //保温层质量
              Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                //涂层质量
              useDate, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),               //涂层使用时间
              plantInfo.get(0).get("bottomLinkType").toString(),                                       //连接形式
              
             Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),                   //SCC损伤机理
             Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                            //检验次数
             plantTest.get(0).get("bottomCheckValidity").toString(),                                           //检验有效性

             Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //设备环境
             Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //是否按规定维护
             Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //基础沉降评价
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //是否介质含水
             
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //水中的H2S值
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //水中PH值
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //是否焊后热处理
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //最大布氏硬度
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //水中cl离子含量

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //是否伴热
             Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOH浓度
             Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //是否蒸汽吹扫
             Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //是否进行应力消除
             Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //热历史
             Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //是否有停机保护
             plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //环境含有物
             Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //钢板中硫含量
             Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //碳酸盐浓度
     );
      bottomEdgeFactorMap.put("bottomEdgeOutDamageFactor", bottomEdgeOutDamageFactor);
	 
// 3、边缘板SCC损伤因子

      double bottomEdgeSCCDamageFactor = calculate.getSCCDamageFactor(
    		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //操作温度
    		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //设计温度
    		  Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),            //SCC损伤机理
    		  Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                 //检验次数
              plantTest.get(0).get("bottomCheckValidity").toString(),                               //检验有效性
              0,                                                                                  //计算外部损伤因子的时候不需要计算敏感性高低，直接输入严重程度指数
             
              Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //是否介质含水
              Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //水中的硫化氢含量
              Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //水中pH值
              Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //是否焊后热处理
              Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //最大布氏硬度
              Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //水中的氯离子含量

              Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //是否伴热
    		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOH浓度
    		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //是否蒸汽吹扫
    		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //是否进行应力消除
    		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //热历史
    		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //是否停机保护
    		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //环境含有物
    		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //钢板中硫含量
    		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //碳酸盐浓度
          );
          bottomEdgeFactorMap.put("bottomEdgeSCCDamageFactor", bottomEdgeSCCDamageFactor);
         
//        4、边缘板脆性断裂因子
          bottomEdgeFactorMap.put("bottomEdgeBrittleDamageFactor", 0);
       
        //------------------------------------计算损伤因子-----------------------------------------------------------

//        计算得到损伤因子的值
   double  bottomEdgeDamageFactor = calculate.getDamageFaction(
            1,                                     //腐蚀类型，壁板为均匀腐蚀，底板为局部腐蚀 0代表均匀腐蚀 1代表局部腐蚀
            bottomEdgeReductionDamageFactor,     //减薄损伤因子
            bottomEdgeOutDamageFactor,           //外部损伤因子
            bottomEdgeSCCDamageFactor,           //应力腐蚀开裂损伤因子
            0             //脆性断裂损伤因子
        );
   bottomEdgeFactorMap.put("bottomEdgeDamageFactor", bottomEdgeDamageFactor);
   bottomEdgeFactor.add(bottomEdgeFactorMap);        

	 
	 //获取损伤因子最大的值测点和值
	 if(bottomEdgeDamageFactor > maxBottomEdgeDamageFactor ){
		 maxBottomEdgeDamageFactor        = bottomEdgeDamageFactor;
		 maxBottomEdgeValueLayerId        = bottomEdgeCorrosion.get("layerId").toString();
		 maxBottomEdgeCorrosion           = Double.parseDouble(bottomEdgeCorrosion.get("long_termCorrosion").toString());
	 } else {
		 maxBottomEdgeDamageFactor        = bottomEdgeDamageFactor;
		 maxBottomEdgeValueLayerId        = bottomEdgeCorrosion.get("layerId").toString();
		 maxBottomEdgeCorrosion           = Double.parseDouble(bottomEdgeCorrosion.get("long_termCorrosion").toString());
	 }
 } 
 
 double  maxBottomMiddleDamageFactor    = 0;
 double  maxBottomMiddleFailConsequence = 0;
 double  maxBottomMiddleCorrosion       = 0;
 String  maxBottomMiddleValueLayerId        = "";
 
 List<Map<String, Object>> bottomMiddleFactor = new ArrayList<Map<String, Object>>(); 
 
 for (Map<String, Object> bottomMiddleCorrosion : bottomMiddlePlantCorrosion)  
 {  
	 Map<String, Object>    bottomMiddleFactorMap = new HashMap<String, Object>(); 
//-------------------------------------中间板风险----------------------------------------
//   1、中间板减薄次因子
	 double bottomMiddleReductionDamageFactor = calculate.getReductionDamageFactor(
             
                 1,                                                                                       //代表壁板
                 Double.parseDouble(bottomMiddleCorrosion.get("long_termCorrosion").toString()),            //该层壁板腐蚀速率
                 useDate,                                                                                     //使用时间(储罐使用时间)
                 Double.parseDouble(plantInfo.get(0).get("bottomMiddleNamelyThickness").toString()),               //壁板初始厚度(目前是名义厚度)

                 Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                      //检验次数
                 plantTest.get(0).get("bottomCheckValidity").toString(),                                    //检验有效性
                 
                 plantInfo.get(0).get("bottomLinkType").toString(),                                    //壁板连接形式
                 Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //是否按规定维护
                 Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //基础沉降评价
            
         );
	 bottomMiddleFactorMap.put("bottomMiddleReductionDamageFactor", bottomMiddleReductionDamageFactor);
	 
//   2、边缘板外部损伤因子
      double bottomMiddleOutDamageFactor  = calculate.getOutDamageFactor(
    		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //操作温度
    		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //设计温度
              1,                       //代表壁板
              Double.parseDouble(plantInfo.get(0).get("bottomMiddleNamelyThickness").toString()),                    //壁板初始厚度(目前是名义厚度)

              Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //管道复杂度
              Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //是否保温层含氯
              Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //是否管支架补偿
              Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //是否界面补偿
              Integer.parseInt(plantMech.get(0).get("floorOutDamageMechanismId").toString()),                //外部损伤机理
             
              useDate,                                                                                        //使用时间(储罐使用时间)
              0,                                                                                          //是否有保温层（底板没有保温层）
              Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),               //保温层质量
              Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                //涂层质量
              useDate, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),               //涂层使用时间
              plantInfo.get(0).get("bottomLinkType").toString(),                                       //连接形式
              
             Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),                   //SCC损伤机理
             Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                            //检验次数
             plantTest.get(0).get("bottomCheckValidity").toString(),                                           //检验有效性

             Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //设备环境
             Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //是否按规定维护
             Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //基础沉降评价
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //是否介质含水
             
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //水中的H2S值
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //水中PH值
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //是否焊后热处理
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //最大布氏硬度
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //水中cl离子含量

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //是否伴热
             Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOH浓度
             Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //是否蒸汽吹扫
             Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //是否进行应力消除
             Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //热历史
             Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //是否有停机保护
             plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //环境含有物
             Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //钢板中硫含量
             Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //碳酸盐浓度
     );
      bottomMiddleFactorMap.put("bottomMiddleOutDamageFactor", bottomMiddleOutDamageFactor);
	 
// 3、壁板SCC损伤因子

      double bottomMiddleSCCDamageFactor = calculate.getSCCDamageFactor(
    		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //操作温度
    		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //设计温度
    		  Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),            //SCC损伤机理
    		  Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                 //检验次数
              plantTest.get(0).get("bottomCheckValidity").toString(),                               //检验有效性
              0,                                                                                  //计算外部损伤因子的时候不需要计算敏感性高低，直接输入严重程度指数
             
              Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //是否介质含水
              Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //水中的硫化氢含量
              Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //水中pH值
              Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //是否焊后热处理
              Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //最大布氏硬度
              Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //水中的氯离子含量

              Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //是否伴热
    		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOH浓度
    		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //是否蒸汽吹扫
    		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //是否进行应力消除
    		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //热历史
    		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //是否停机保护
    		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //环境含有物
    		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //钢板中硫含量
    		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //碳酸盐浓度
          );
          bottomMiddleFactorMap.put("bottomMiddleSCCDamageFactor", bottomMiddleSCCDamageFactor);
         
//        4、壁板脆性断裂因子
          bottomMiddleFactorMap.put("bottomMiddleBrittleDamageFactor", 0);
       
        //------------------------------------计算损伤因子-----------------------------------------------------------

//        计算得到损伤因子的值
   double  bottomMiddleDamageFactor = calculate.getDamageFaction(
            1,                                     //腐蚀类型，壁板为均匀腐蚀，底板为局部腐蚀 0代表均匀腐蚀 1代表局部腐蚀
            bottomMiddleReductionDamageFactor,     //减薄损伤因子
            bottomMiddleOutDamageFactor,           //外部损伤因子
            bottomMiddleSCCDamageFactor,           //应力腐蚀开裂损伤因子
            0             //脆性断裂损伤因子
        );
   bottomMiddleFactorMap.put("bottomMiddleDamageFactor", bottomMiddleDamageFactor);
           
   bottomMiddleFactor.add(bottomMiddleFactorMap);
	 
	 //获取损伤因子最大的值测点和值
	 if(bottomMiddleDamageFactor > maxBottomEdgeDamageFactor ){
		 maxBottomMiddleDamageFactor        = bottomMiddleDamageFactor;
		 maxBottomMiddleValueLayerId        = bottomMiddleCorrosion.get("layerId").toString();
		 maxBottomMiddleCorrosion           = Double.parseDouble(bottomMiddleCorrosion.get("long_termCorrosion").toString());
	 } else {
		 maxBottomMiddleDamageFactor        = bottomMiddleDamageFactor;
		 maxBottomMiddleValueLayerId        = bottomMiddleCorrosion.get("layerId").toString();
		 maxBottomMiddleCorrosion           = Double.parseDouble(bottomMiddleCorrosion.get("long_termCorrosion").toString());
	 }
 } 
 
//----------------------------底板损伤因子------------------------------------------------------
 
		 String floorMajorRisk    =  "";
		 double floorDamageFactor =  0;
		 
		 if(maxBottomEdgeDamageFactor >= maxBottomMiddleDamageFactor){
		      floorMajorRisk    =  maxBottomEdgeValueLayerId;
		      floorDamageFactor =  maxBottomEdgeDamageFactor;
		 }else{
			  floorMajorRisk    =  maxBottomMiddleValueLayerId;
		      floorDamageFactor =  maxBottomMiddleDamageFactor;
		 }
     
//---------------------------------底板失效后果--------------------------------------
 double floorFailConsequence = calculate.getFailurefloorConsequence(
		   Double.parseDouble(plantInfo.get(0).get("D").toString()),                          //储罐直径
		   Double.parseDouble(plantInfo.get(0).get("fillH").toString()),                      //储罐液面高度
		   Integer.parseInt(plantInfo.get(0).get("sensitiveEnvironment").toString()),       //环境敏感度
		   Double.parseDouble(plantInfo.get(0).get("fMatCost_").toString()),                  //材料价格系数
		   Double.parseDouble(plantInfo.get(0).get("stopLoss").toString()),                   //停产造成的损失
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentage").toString()),         //溢出围堪的流体百分比
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageIn").toString()),       //溢出围堪但仍在罐区内，地表土壤中的流体百分比
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageOut").toString()),      //溢出围堪且已流到罐区外，地表土壤中的流体百分比
	       Double.parseDouble(plantInfo.get(0).get("bottomToWaterDistance").toString()),      //罐底到地下水的距离
	   	   Double.parseDouble(plantInfo.get(0).get("mediumPercentage").toString()),           //介质密度
		   Double.parseDouble(plantInfo.get(0).get("mediumDyViscosity").toString()),          //介质动力粘度
		   Integer.parseInt(plantInfo.get(0).get("bottomGasket").toString()),               //储罐基础形式。0---基础为水泥或沥青  1——基础设有RPB，2——基础没有RPB
		   Integer.parseInt(plantInfo.get(0).get("bottomGasketSoil").toString())           //储罐基础下面土壤类型
       ); 
 
 
 
//找出壁板板最大损伤因子，按照最大损伤因子计算风险等级     
String floorFailConsequenceLevel  =    "A";
int    floorFailConsequenceLevelIndex = 1;
if(floorFailConsequence < fAcceptBaseQ_){
	floorFailConsequenceLevel = "A";
	floorFailConsequenceLevelIndex = 1;
} else if(floorFailConsequence > fAcceptBaseQ_ && floorFailConsequence <= fAcceptBaseQ_*10){
	floorFailConsequenceLevel = "B";
	floorFailConsequenceLevelIndex = 2;
} else if(floorFailConsequence > fAcceptBaseQ_*10 && floorFailConsequence <= fAcceptBaseQ_*100){
	floorFailConsequenceLevel = "C";
	floorFailConsequenceLevelIndex = 3;
} else if(floorFailConsequence > fAcceptBaseQ_*100 && floorFailConsequence <= fAcceptBaseQ_*1000){
	floorFailConsequenceLevel = "D";
	floorFailConsequenceLevelIndex = 4;
} else if(floorFailConsequence > fAcceptBaseQ_*1000){
	floorFailConsequenceLevel = "E";
	floorFailConsequenceLevelIndex = 5;
}

//壁板失效可能性
double floorAverageFailurePro = calculate.getAverageFailurePro(1,  Float.parseFloat(plantInfo.get(0).get("breakSize").toString()));          //壁板平均失效可能性
double floorFailurePro        = calculate.getFailurePro(1, floorAverageFailurePro, floorDamageFactor);  //壁板失效可能性
int    floorFailureProLevel   = 1;
//计算壁板失效可能性等级
if(floorFailurePro > 0 && floorFailurePro <= 0.00001 ){
	floorFailureProLevel = 1;
}else if(floorFailurePro > 0.00001 && floorFailurePro <= 0.00010){
	floorFailureProLevel = 2;
}else if(floorFailurePro > 0.00010 && floorFailurePro <= 0.00100){
	floorFailureProLevel = 3;
}else if(floorFailurePro > 0.00100 && floorFailurePro <= 0.01000){
	floorFailureProLevel = 4;
}else if(floorFailurePro > 0.01000){
	floorFailureProLevel = 5;
}

//------------------------------------------------底板风险等级---------------------------------------------------------

double floorRisk      = floorFailConsequence*floorFailurePro;
String floorRiskLevel = riskMatrixTable[floorFailureProLevel][floorFailConsequenceLevelIndex];


//---------------------------------------------计算未来风险和损伤因子---------------------------------------------------
//---------------------------------------------计算未来风险和损伤因子---------------------------------------------------
//---------------------------------------------计算未来风险和损伤因子---------------------------------------------------
//---------------------------------------------计算未来风险和损伤因子---------------------------------------------------
//---------------------------------------------计算未来风险和损伤因子---------------------------------------------------

List<Map<String, Object>> plantFactor_Fu = new ArrayList<Map<String, Object>>(); 


for(int j=0;j<5;j++){
	Map<String, Object>    plantFactorMap_Fu = new HashMap<String, Object>(); 
	double  maxWallDamageFactor_Fu    = 0;
    double  maxWallFailConsequence_Fu = 0;
     
    for (Map<String, Object> WallPlantCorrosion : wallPlantCorrosion)  
    {  
//-------------------------------------计算壁板损伤因子----------------------------------------
//      1、壁板板减薄次因子
   	 double wallReductionDamageFactor_Fu = calculate.getReductionDamageFactor(
                
                    1,                                                             //代表壁板
                    Double.parseDouble(WallPlantCorrosion.get("long_termCorrosion").toString()),            //该层壁板腐蚀速率
                    useDate + 1 + j,                                                                                    //使用时间(储罐使用时间)
                    Double.parseDouble(WallPlantCorrosion.get("namelyThickness").toString()),               //壁板初始厚度(目前是名义厚度)

                    Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                      //检验次数
                    plantTest.get(0).get("wallCheckValidity").toString(),                                    //检验有效性
                    
                    plantInfo.get(0).get("wallboardLinkType").toString(),                                    //壁板连接形式
                    Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //是否按规定维护
                    Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //基础沉降评价
               
            );
   	 
//      2、壁板外部损伤因子
         double wallOutDamageFactor_Fu  = calculate.getOutDamageFactor(
       		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //操作温度
       		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //设计温度
                 1,                                                                                            //代表壁板
                 Double.parseDouble(WallPlantCorrosion.get("namelyThickness").toString()),                    //壁板初始厚度(目前是名义厚度)

                 Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //管道复杂度
                 Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //是否保温层含氯
                 Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //是否管支架补偿
                 Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //是否界面补偿
                 Integer.parseInt(plantMech.get(0).get("wallOutDamageMechanismId").toString()),                //外部损伤机理
                
                 useDate + 1 + j,                                                                                        //使用时间(储罐使用时间)
                 Integer.parseInt(plantInfo.get(0).get("isWallboardKeepWarm").toString()),                                //是否有保温层
                 Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),                            //保温层质量
                 Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                             //涂层质量
                 useDate + 1 + j, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),                         //涂层使用时间
                 plantInfo.get(0).get("wallboardLinkType").toString(),                                       //连接形式
                 
                Integer.parseInt(plantMech.get(0).get("wallSCCMechanismId").toString()),                   //SCC损伤机理
                Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                            //检验次数
                plantTest.get(0).get("wallCheckValidity").toString(),                                           //检验有效性

                Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //设备环境
                Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //是否按规定维护
                Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //基础沉降评价
                Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //是否介质含水
                
                Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //水中的H2S值
                Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //水中PH值
                Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //是否焊后热处理
                Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //最大布氏硬度
                Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //水中cl离子含量

                Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //是否伴热
                Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOH浓度
                Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //是否蒸汽吹扫
                Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //是否进行应力消除
                Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //热历史
                Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //是否有停机保护
                plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //环境含有物
                Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //钢板中硫含量
                Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //碳酸盐浓度
        );
   	 
//    3、壁板SCC损伤因子

         double wallSCCDamageFactor_Fu = calculate.getSCCDamageFactor(
       		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //操作温度
       		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //设计温度
       		  Integer.parseInt(plantMech.get(0).get("wallSCCMechanismId").toString()),            //SCC损伤机理
       		  Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                 //检验次数
             plantTest.get(0).get("wallCheckValidity").toString(),                               //检验有效性
             0,                                                                                  //计算外部损伤因子的时候不需要计算敏感性高低，直接输入严重程度指数
            
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //是否介质含水
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //水中的硫化氢含量
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //水中pH值
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //是否焊后热处理
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //最大布氏硬度
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //水中的氯离子含量

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //是否伴热
       		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOH浓度
       		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //是否蒸汽吹扫
       		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //是否进行应力消除
       		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //热历史
       		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //是否停机保护
       		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //环境含有物
       		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //钢板中硫含量
       		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //碳酸盐浓度
             );       
            
//           4、壁板脆性断裂因子
//             wallFactorMap.put("wallBrittleDamageFactor", 0);
          
           //------------------------------------计算损伤因子-----------------------------------------------------------

//           计算得到损伤因子的值
      double  wallDamageFactor_Fu = calculate.getDamageFaction(
               0,                                     //腐蚀类型，壁板为均匀腐蚀，底板为局部腐蚀 0代表均匀腐蚀 1代表局部腐蚀
               wallReductionDamageFactor_Fu,     //减薄损伤因子
               wallOutDamageFactor_Fu,           //外部损伤因子
               wallSCCDamageFactor_Fu,           //应力腐蚀开裂损伤因子
               0             //脆性断裂损伤因子
           );
      
              
//------------------------------壁板失效后果----------------------------------------------------------
      double wallFailConsequence_Fu = calculate.getFailureWallConsequence(
   		   Double.parseDouble(plantInfo.get(0).get("D").toString()),                            //罐直径
   		   Double.parseDouble(WallPlantCorrosion.get("height").toString()),         //储罐单层壁板高度
   		   Integer.parseInt(WallPlantCorrosion.get("layerNO").toString()),           //第几层壁板
           Integer.parseInt(plantInfo.get(0).get("sensitiveEnvironment").toString()),             //失效后果可接受的基准 单位：万元
   		   Double.parseDouble(plantInfo.get(0).get("fillH").toString()),                         //泄露孔上方的液体高度
   		   Double.parseDouble(plantInfo.get(0).get("fMatCost_").toString()),                     //材料价格系数
   		   Double.parseDouble(plantInfo.get(0).get("stopLoss").toString()),                      //停产造成的损失
   		   Double.parseDouble(plantInfo.get(0).get("overflowPercentage").toString()),            //溢出围堰的流体百分比
   		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageIn").toString()),          //溢出围堪但仍在罐区内，地表土壤中的流体百分比
   		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageOut").toString())          //溢出围堪且已流到罐区外，地表土壤中的流体百分比
          ); 
      
   	 
//--------------------------获取损伤因子最大的值测点和值-------------------------------------------------
   	 if(wallDamageFactor_Fu > maxWallDamageFactor){
   		 maxWallDamageFactor_Fu    = wallDamageFactor_Fu;
   		 maxWallFailConsequence_Fu = wallFailConsequence_Fu;
   	 } else {
   		maxWallDamageFactor_Fu    = wallDamageFactor_Fu;
  		maxWallFailConsequence_Fu = wallFailConsequence_Fu;
   	 }
    } 
    plantFactorMap_Fu.put("maxWallDamageFactor_Fu", maxWallDamageFactor_Fu);
    plantFactorMap_Fu.put("maxWallFailConsequence_Fu", maxWallFailConsequence_Fu);
//-----------------------------壁板损伤因子---------------------------------------------------------
//  找出壁板板最大损伤因子，按照最大损伤因子计算风险等级     
 String wallFailConsequenceLevel_Fu  =    "A";
 int    wallFailConsequenceLevelIndex_Fu = 1;
 if(maxWallFailConsequence_Fu < fAcceptBaseQ_){
	 wallFailConsequenceLevel_Fu = "A";
	 wallFailConsequenceLevelIndex_Fu = 1;
 } else if(maxWallFailConsequence_Fu > fAcceptBaseQ_ && maxWallFailConsequence_Fu <= fAcceptBaseQ_*10){
	 wallFailConsequenceLevel_Fu = "B";
	 wallFailConsequenceLevelIndex_Fu = 2;
 } else if(maxWallFailConsequence_Fu > fAcceptBaseQ_*10 && maxWallFailConsequence_Fu <= fAcceptBaseQ_*100){
	 wallFailConsequenceLevel_Fu = "C";
	 wallFailConsequenceLevelIndex_Fu = 3;
 } else if(maxWallFailConsequence_Fu > fAcceptBaseQ_*100 && maxWallFailConsequence_Fu <= fAcceptBaseQ_*1000){
	 wallFailConsequenceLevel_Fu = "D";
	 wallFailConsequenceLevelIndex_Fu = 4;
 } else if(maxWallFailConsequence_Fu > fAcceptBaseQ_*1000){
	 wallFailConsequenceLevel_Fu = "E";
	 wallFailConsequenceLevelIndex_Fu = 5;
 }
 
//壁板失效可能性
double wallFailurePro_Fu        = calculate.getFailurePro(1, wallAverageFailurePro, maxWallDamageFactor_Fu);  //壁板失效可能性
int    wallFailureProLevel_Fu   = 1;
//计算壁板失效可能性等级
if(wallFailurePro_Fu > 0 && wallFailurePro_Fu <= 0.00001 ){
	wallFailureProLevel_Fu = 1;
}else if(wallFailurePro_Fu > 0.00001 && wallFailurePro_Fu <= 0.00010){
	wallFailureProLevel_Fu = 2;
}else if(wallFailurePro_Fu > 0.00010 && wallFailurePro_Fu <= 0.00100){
	wallFailureProLevel_Fu = 3;
}else if(wallFailurePro_Fu > 0.00100 && wallFailurePro_Fu <= 0.01000){
	wallFailureProLevel_Fu = 4;
}else if(wallFailurePro_Fu > 0.01000){
	wallFailureProLevel_Fu = 5;
}
plantFactorMap_Fu.put("wallFailurePro_Fu", wallFailurePro_Fu);

//-----------------------------------------计算底板风险等级--------------------------------------

double wallRisk_Fu      = maxWallFailConsequence_Fu * wallFailurePro_Fu;                              //壁板风险
String wallRiskLevel_Fu = riskMatrixTable[wallFailureProLevel_Fu][wallFailConsequenceLevelIndex_Fu];  //壁板风险等级

plantFactorMap_Fu.put("wallRisk_Fu", wallRisk_Fu);
plantFactorMap_Fu.put("wallRiskLevel_Fu", wallRiskLevel_Fu);

//-------------------------底板风险计算分割线-----------------------------------------------------


double  maxBottomEdgeDamageFactor_Fu        = 0;
double  maxBottomEdgeFailConsequence_Fu     = 0;
String  maxBottomEdgeValueLayerId_Fu        = "";

for (Map<String, Object>  bottomEdgeCorrosion : bottomEdgePlantCorrosion)  
{  
//-------------------------------------边缘板风险----------------------------------------
//  1、边缘板减薄次因子
	 double bottomEdgeReductionDamageFactor_Fu = calculate.getReductionDamageFactor(
            
                1,                                                                                       //代表壁板
                Double.parseDouble(bottomEdgeCorrosion.get("long_termCorrosion").toString()),            //该层壁板腐蚀速率
                useDate + 1 + j,                                                                                     //使用时间(储罐使用时间)
                Double.parseDouble(plantInfo.get(0).get("bottomEdgeNamelyThickness").toString()),               //壁板初始厚度(目前是名义厚度)

                Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                      //检验次数
                plantTest.get(0).get("bottomCheckValidity").toString(),                                    //检验有效性
                
                plantInfo.get(0).get("bottomLinkType").toString(),                                    //壁板连接形式
                Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //是否按规定维护
                Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //基础沉降评价
           
        );
	 
//  2、边缘板外部损伤因子
     double bottomEdgeOutDamageFactor_Fu  = calculate.getOutDamageFactor(
	   		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //操作温度
	   		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //设计温度
             1,                       //代表壁板
             Double.parseDouble(plantInfo.get(0).get("bottomEdgeNamelyThickness").toString()),                    //壁板初始厚度(目前是名义厚度)

             Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //管道复杂度
             Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //是否保温层含氯
             Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //是否管支架补偿
             Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //是否界面补偿
             Integer.parseInt(plantMech.get(0).get("floorOutDamageMechanismId").toString()),                //外部损伤机理
            
             useDate + 1 + j,                                                                                        //使用时间(储罐使用时间)
             0,                                                                                          //是否有保温层（底板没有保温层）
             Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),               //保温层质量
             Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                //涂层质量
             useDate + 1 + j, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),               //涂层使用时间
             plantInfo.get(0).get("bottomLinkType").toString(),                                       //连接形式
             
            Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),                   //SCC损伤机理
            Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                            //检验次数
            plantTest.get(0).get("bottomCheckValidity").toString(),                                           //检验有效性

            Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //设备环境
            Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //是否按规定维护
            Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //基础沉降评价
            Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //是否介质含水
            
            Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //水中的H2S值
            Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //水中PH值
            Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //是否焊后热处理
            Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //最大布氏硬度
            Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //水中cl离子含量

            Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //是否伴热
            Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOH浓度
            Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //是否蒸汽吹扫
            Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //是否进行应力消除
            Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //热历史
            Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //是否有停机保护
            plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //环境含有物
            Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //钢板中硫含量
            Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //碳酸盐浓度
    );
	 
//3、边缘板SCC损伤因子

     double bottomEdgeSCCDamageFactor_Fu = calculate.getSCCDamageFactor(
   		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //操作温度
   		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //设计温度
   		  Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),            //SCC损伤机理
   		  Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                 //检验次数
             plantTest.get(0).get("bottomCheckValidity").toString(),                               //检验有效性
             0,                                                                                  //计算外部损伤因子的时候不需要计算敏感性高低，直接输入严重程度指数
            
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //是否介质含水
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //水中的硫化氢含量
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //水中pH值
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //是否焊后热处理
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //最大布氏硬度
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //水中的氯离子含量

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //是否伴热
   		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOH浓度
   		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //是否蒸汽吹扫
   		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //是否进行应力消除
   		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //热历史
   		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //是否停机保护
   		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //环境含有物
   		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //钢板中硫含量
   		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //碳酸盐浓度
         );
        
//       4、边缘板脆性断裂因子
//         bottomEdgeFactorMap.put("bottomEdgeBrittleDamageFactor", 0);
      
       //------------------------------------计算损伤因子-----------------------------------------------------------

//       计算得到损伤因子的值
  double  bottomEdgeDamageFactor_Fu = calculate.getDamageFaction(
           1,                                     //腐蚀类型，壁板为均匀腐蚀，底板为局部腐蚀 0代表均匀腐蚀 1代表局部腐蚀
           bottomEdgeReductionDamageFactor_Fu,     //减薄损伤因子
           bottomEdgeOutDamageFactor_Fu,           //外部损伤因子
           bottomEdgeSCCDamageFactor_Fu,           //应力腐蚀开裂损伤因子
           0             //脆性断裂损伤因子
       );     

	 
	 //获取损伤因子最大的值测点和值
	 if(bottomEdgeDamageFactor_Fu > maxBottomEdgeDamageFactor_Fu ){
		 maxBottomEdgeDamageFactor_Fu        = bottomEdgeDamageFactor_Fu;
	 } else {
		 maxBottomEdgeDamageFactor_Fu        = bottomEdgeDamageFactor_Fu;
	 }
} 

double  maxBottomMiddleDamageFactor_Fu    = 0;
double  maxBottomMiddleFailConsequence_Fu = 0;

for (Map<String, Object> bottomMiddleCorrosion : bottomMiddlePlantCorrosion)  
{  
//-------------------------------------中间板风险----------------------------------------
//  1、中间板减薄次因子
	 double bottomMiddleReductionDamageFactor_Fu = calculate.getReductionDamageFactor(
            
                1,                                                                                       //代表壁板
                Double.parseDouble(bottomMiddleCorrosion.get("long_termCorrosion").toString()),            //该层壁板腐蚀速率
                useDate + 1 + j,                                                                                     //使用时间(储罐使用时间)
                Double.parseDouble(plantInfo.get(0).get("bottomMiddleNamelyThickness").toString()),               //壁板初始厚度(目前是名义厚度)

                Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                      //检验次数
                plantTest.get(0).get("bottomCheckValidity").toString(),                                    //检验有效性
                
                plantInfo.get(0).get("bottomLinkType").toString(),                                    //壁板连接形式
                Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //是否按规定维护
                Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //基础沉降评价
           
        );
	 
//  2、边缘板外部损伤因子
     double bottomMiddleOutDamageFactor_Fu  = calculate.getOutDamageFactor(
   		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //操作温度
   		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //设计温度
             1,                       //代表壁板
             Double.parseDouble(plantInfo.get(0).get("bottomMiddleNamelyThickness").toString()),                    //壁板初始厚度(目前是名义厚度)

             Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //管道复杂度
             Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //是否保温层含氯
             Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //是否管支架补偿
             Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //是否界面补偿
             Integer.parseInt(plantMech.get(0).get("floorOutDamageMechanismId").toString()),                //外部损伤机理
            
             useDate + 1 + j,                                                                                        //使用时间(储罐使用时间)
             0,                                                                                          //是否有保温层（底板没有保温层）
             Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),               //保温层质量
             Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                //涂层质量
             useDate + 1 +j, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),               //涂层使用时间
             plantInfo.get(0).get("bottomLinkType").toString(),                                       //连接形式
             
            Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),                   //SCC损伤机理
            Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                            //检验次数
            plantTest.get(0).get("bottomCheckValidity").toString(),                                           //检验有效性

            Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //设备环境
            Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //是否按规定维护
            Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //基础沉降评价
            Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //是否介质含水
            
            Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //水中的H2S值
            Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //水中PH值
            Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //是否焊后热处理
            Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //最大布氏硬度
            Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //水中cl离子含量

            Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //是否伴热
            Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOH浓度
            Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //是否蒸汽吹扫
            Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //是否进行应力消除
            Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //热历史
            Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //是否有停机保护
            plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //环境含有物
            Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //钢板中硫含量
            Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //碳酸盐浓度
    );
	 
//3、壁板SCC损伤因子

     double bottomMiddleSCCDamageFactor_Fu = calculate.getSCCDamageFactor(
   		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //操作温度
   		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //设计温度
   		  Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),            //SCC损伤机理
   		  Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                 //检验次数
             plantTest.get(0).get("bottomCheckValidity").toString(),                               //检验有效性
             0,                                                                                  //计算外部损伤因子的时候不需要计算敏感性高低，直接输入严重程度指数
            
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //是否介质含水
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //水中的硫化氢含量
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //水中pH值
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //是否焊后热处理
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //最大布氏硬度
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //水中的氯离子含量

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //是否伴热
   		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOH浓度
   		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //是否蒸汽吹扫
   		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //是否进行应力消除
   		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //热历史
   		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //是否停机保护
   		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //环境含有物
   		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //钢板中硫含量
   		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //碳酸盐浓度
         );
        
//       4、壁板脆性断裂因子
//         bottomMiddleFactorMap.put("bottomMiddleBrittleDamageFactor", 0);
      
       //------------------------------------计算损伤因子-----------------------------------------------------------

//       计算得到损伤因子的值
  double  bottomMiddleDamageFactor_Fu = calculate.getDamageFaction(
           1,                                     //腐蚀类型，壁板为均匀腐蚀，底板为局部腐蚀 0代表均匀腐蚀 1代表局部腐蚀
           bottomMiddleReductionDamageFactor_Fu,     //减薄损伤因子
           bottomMiddleOutDamageFactor_Fu,           //外部损伤因子
           bottomMiddleSCCDamageFactor_Fu,           //应力腐蚀开裂损伤因子
           0             //脆性断裂损伤因子
       );
          
	 
	 //获取损伤因子最大的值测点和值
	 if(bottomMiddleDamageFactor_Fu > maxBottomEdgeDamageFactor_Fu ){
		 maxBottomMiddleDamageFactor_Fu        = bottomMiddleDamageFactor_Fu;
	 } else {
		 maxBottomMiddleDamageFactor_Fu        = bottomMiddleDamageFactor_Fu;
	 }
} 

//----------------------------底板损伤因子------------------------------------------------------

	 double floorDamageFactor_Fu =  0;
	 
	 if(maxBottomEdgeDamageFactor_Fu >= maxBottomMiddleDamageFactor_Fu){
	      floorDamageFactor_Fu =  maxBottomEdgeDamageFactor_Fu;
	 }else{
	      floorDamageFactor_Fu =  maxBottomMiddleDamageFactor_Fu;
	 }
    
//---------------------------------底板失效后果--------------------------------------
	 double floorFailConsequence_Fu = calculate.getFailurefloorConsequence(
		   Double.parseDouble(plantInfo.get(0).get("D").toString()),                          //储罐直径
		   Double.parseDouble(plantInfo.get(0).get("fillH").toString()),                      //储罐液面高度
		   Integer.parseInt(plantInfo.get(0).get("sensitiveEnvironment").toString()),       //环境敏感度
		   Double.parseDouble(plantInfo.get(0).get("fMatCost_").toString()),                  //材料价格系数
		   Double.parseDouble(plantInfo.get(0).get("stopLoss").toString()),                   //停产造成的损失
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentage").toString()),         //溢出围堪的流体百分比
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageIn").toString()),       //溢出围堪但仍在罐区内，地表土壤中的流体百分比
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageOut").toString()),      //溢出围堪且已流到罐区外，地表土壤中的流体百分比
	       Double.parseDouble(plantInfo.get(0).get("bottomToWaterDistance").toString()),      //罐底到地下水的距离
	   	   Double.parseDouble(plantInfo.get(0).get("mediumPercentage").toString()),           //介质密度
		   Double.parseDouble(plantInfo.get(0).get("mediumDyViscosity").toString()),          //介质动力粘度
		   Integer.parseInt(plantInfo.get(0).get("bottomGasket").toString()),               //储罐基础形式。0---基础为水泥或沥青  1——基础设有RPB，2——基础没有RPB
		   Integer.parseInt(plantInfo.get(0).get("bottomGasketSoil").toString())           //储罐基础下面土壤类型
      ); 

	plantFactorMap_Fu.put("floorDamageFactor_Fu", floorDamageFactor_Fu);
	plantFactorMap_Fu.put("floorFailConsequence_Fu", floorFailConsequence_Fu);
	
	//找出壁板板最大损伤因子，按照最大损伤因子计算风险等级     
	String floorFailConsequenceLevel_Fu  =    "A";
	int    floorFailConsequenceLevelIndex_Fu = 1;
	if(floorFailConsequence_Fu < fAcceptBaseQ_){
		floorFailConsequenceLevel_Fu = "A";
		floorFailConsequenceLevelIndex_Fu = 1;
	} else if(floorFailConsequence_Fu > fAcceptBaseQ_ && floorFailConsequence_Fu <= fAcceptBaseQ_*10){
		floorFailConsequenceLevel_Fu = "B";
		floorFailConsequenceLevelIndex_Fu = 2;
	} else if(floorFailConsequence_Fu > fAcceptBaseQ_*10 && floorFailConsequence_Fu <= fAcceptBaseQ_*100){
		floorFailConsequenceLevel_Fu = "C";
		floorFailConsequenceLevelIndex_Fu = 3;
	} else if(floorFailConsequence_Fu > fAcceptBaseQ_*100 && floorFailConsequence_Fu <= fAcceptBaseQ_*1000){
		floorFailConsequenceLevel_Fu = "D";
		floorFailConsequenceLevelIndex_Fu = 4;
	} else if(floorFailConsequence_Fu > fAcceptBaseQ_*1000){
		floorFailConsequenceLevel_Fu = "E";
		floorFailConsequenceLevelIndex_Fu = 5;
	}
	
	//壁板失效可能性
	double floorFailurePro_Fu        = calculate.getFailurePro(1, floorAverageFailurePro, floorDamageFactor_Fu);  //壁板失效可能性
	int    floorFailureProLevel_Fu   = 1;
	//计算壁板失效可能性等级
	if(floorFailurePro_Fu > 0 && floorFailurePro_Fu <= 0.00001 ){
		floorFailureProLevel_Fu = 1;
	}else if(floorFailurePro_Fu > 0.00001 && floorFailurePro_Fu <= 0.00010){
		floorFailureProLevel_Fu = 2;
	}else if(floorFailurePro_Fu > 0.00010 && floorFailurePro_Fu <= 0.00100){
		floorFailureProLevel_Fu = 3;
	}else if(floorFailurePro_Fu > 0.00100 && floorFailurePro_Fu <= 0.01000){
		floorFailureProLevel_Fu = 4;
	}else if(floorFailurePro_Fu > 0.01000){
		floorFailureProLevel_Fu = 5;
	}
	
	plantFactorMap_Fu.put("floorFailurePro_Fu", floorFailurePro_Fu);
	
	//------------------------------------------------底板风险等级---------------------------------------------------------
	
	double floorRisk_Fu      = floorFailConsequence_Fu*floorFailurePro_Fu;
	String floorRiskLevel_Fu = riskMatrixTable[floorFailureProLevel_Fu][floorFailConsequenceLevelIndex_Fu];
	
	plantFactorMap_Fu.put("floorRisk_Fu", floorRisk_Fu);
	plantFactorMap_Fu.put("floorRiskLevel_Fu", floorRiskLevel_Fu);
	plantFactor_Fu.add(plantFactorMap_Fu);
}


//-------------------------------计算下次检验时间---------------------------------
	double thresholdRisk = Integer.parseInt(plantInfo.get(0).get("thresholdRisk").toString());
	String wallNextCheckDate  = "";
	String floorNextCheckDate = "";
	int    ii                 = 0;
	for (Map<String, Object> plantFactor : plantFactor_Fu)  
	{  
	  double wallDamageFactor_Fu = Double.parseDouble(plantFactor.get("maxWallDamageFactor_Fu").toString());
	  if(wallDamageFactor_Fu > thresholdRisk){
		  wallNextCheckDate = calculate.addDate(ii);
	  }else{
		  wallNextCheckDate = calculate.addDate(4);
	  }
	  
	  double floorDamageFactor_Fut = Double.parseDouble(plantFactor.get("floorDamageFactor_Fu").toString());
	  if(floorDamageFactor_Fut > thresholdRisk){
	      floorNextCheckDate = calculate.addDate(ii);
	  }else{
	      floorNextCheckDate = calculate.addDate(ii);
	  }
	  ii++;
	}


//-------------------------------------风险结果保存--------------------------------------
//-------------------------------------风险结果保存--------------------------------------
//-------------------------------------风险结果保存--------------------------------------
	
	
	try {  
        Class.forName(name);//指定连接类型  
        conn = DriverManager.getConnection(url, user, password);  //获取连接  
        stmt = conn.createStatement();                            //创建Statement对象
        
        //查询该设备风险列表是否已存在
        ResultSet rs1 = stmt.executeQuery("select * from tb_riskrecordlist where pid="+PlantId);//创建数据对象
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        int re2 = 0;
        String  wallEvaDate = df.format(new Date());
        String floorEvaDate = df.format(new Date());
        if(rs1.next()){   //表明结果集不为空
            //原来已经有记录，则更新
        	  re2 = stmt.executeUpdate("update tb_riskrecordlist set wallEvaDate='"
            +wallEvaDate+"', floorEvaDate='"+floorEvaDate+"' where pid="+PlantId);//创建数据对象             	
        }else{
        	//原来没有记录，则新增
         	  re2 = stmt.executeUpdate("insert into tb_riskrecordlist set pid="+PlantId
         			 +",wallEvaDate='"+wallEvaDate+"', floorEvaDate='"+floorEvaDate+"'");//创建数据对象
        }
        
//        //查询设备相关的风险记录的id值
//        int riskRecordListId = 0;
//        if(re2>0){
//        	ResultSet rs2 = stmt.executeQuery("select id from tb_riskrecordlist where pid="+PlantId);//创建数据对象
//        	while(rs2.next()){
//        		riskRecordListId = rs2.getInt("id");
//        	}
//        	rs2.close();
//        } 
        
        //保存风险记录列表
        PreparedStatement pstmt = conn.prepareStatement("insert into tb_risklist set "
        		+ "pid="+PlantId+","                       //设备Id
        		+ "factoryId='"+plantInfo.get(0).get("factoryId").toString()+"',"           //厂区名称  
        		+ "workshopId='"+plantInfo.get(0).get("workshopId").toString()+"',"          //车间名称
        		+ "areaId='"+plantInfo.get(0).get("areaId").toString()+"',"              //区域名称
        		+ "plantNO='"+plantInfo.get(0).get("plantNO").toString()+"',"             //设备位号
        		+ "plantName='"+plantInfo.get(0).get("plantName").toString()+"',"            //设备名称
        		+ "countDate='"+df.format(new Date())+"',"            //计算时间
        		
        		+ "wallMajorRisk='"+maxValueLayerId+"',"            //壁板风险最大测点
        		+ "floorMajorRisk='"+floorMajorRisk+"',"            //floorMajorRisk
        		+ "wallDamageFactor='"+maxWallDamageFactor+"',"            //wallDamageFactor
        		+ "floorDamageFactor='"+floorDamageFactor+"',"            //floorDamageFactor
        		
        		+ "floorEdgeDamageFactor='"+maxBottomEdgeDamageFactor+"',"            //底板边缘板损伤因子最大值
        		+ "floorEdgeCorrosionSpeed='"+maxBottomEdgeCorrosion+"',"            //底板边缘板损伤因子最大值对应的腐蚀速率
        		+ "floorMiddleDamageFactor='"+maxBottomMiddleDamageFactor+"',"            //底板中间板损伤因子最大值
        		+ "floorMiddleCorrosionSpeed='"+maxBottomMiddleCorrosion+"',"            //底板中间板损伤因子最大值对应的腐蚀速率
        		
        		+ "wallRisk='"+wallRisk+"',"            //壁板风险
        		+ "wallRiskLevel='"+wallRiskLevel+"',"            //壁板风险等级
        		+ "floorRisk='"+floorRisk+"',"            //底板风险
        		+ "floorRiskLevel='"+floorRiskLevel+"',"            //底板风险等级
        		
        		+ "wallFailPro='"+wallFailurePro+"',"            //壁板失效可能性
        		+ "wallFailProLevel='"+wallFailureProLevel+"',"            //壁板失效可能性等级
        		+ "floorFailPro='"+floorFailurePro+"',"            //底板失效可能性
        		+ "floorFailProLevel='"+floorFailureProLevel+"',"            //底板失效可能性等级
        		+ "wallConsequence='"+maxWallFailConsequence+"',"            //壁板失效后果
        		+ "floorConsequence='"+floorFailConsequence+"',"            //底板失效后果
        		+ "wallConsequenceLevel='"+wallFailConsequenceLevel+"',"            //底板失效后果等级
        		+ "floorConsequenceLevel='"+floorFailConsequenceLevel+"',"            //底板失效后果等级
        		
        		+ "wallFailPro_fu="+Double.parseDouble(plantFactor_Fu.get(4).get("wallFailurePro_Fu").toString())+","            //壁板未来失效可能性
        		+ "floorFailPro_fu="+Double.parseDouble(plantFactor_Fu.get(4).get("floorFailurePro_Fu").toString())+","            //底板未来失效可能性
        		+ "wallRisk_fu="+Double.parseDouble(plantFactor_Fu.get(4).get("wallRisk_Fu").toString())+","            //壁板未来风险
        		+ "floorRisk_fu="+Double.parseDouble(plantFactor_Fu.get(4).get("floorRisk_Fu").toString())+","            //底板未来风险
        		+ "wallRiskLevel_fu='"+plantFactor_Fu.get(4).get("wallRiskLevel_Fu").toString()+"',"            //壁板未来风险等级
        		+ "floorRiskLevel_fu='"+plantFactor_Fu.get(4).get("floorRiskLevel_Fu").toString()+"',"            //底板未来风险等级
        		
        		+ "wallNextCheckDate='"+wallNextCheckDate+"',"            //壁板未来风险等级
        		+ "floorNextCheckDate='"+floorNextCheckDate+"',"            //底板未来风险等级
        		
        		+ "wallDamageFactor_trendYear='"
        		+calculate.addDate(0)+","
        		+calculate.addDate(1)+"," 
        		+calculate.addDate(2)+"," 
        		+calculate.addDate(3)+"," 
        		+calculate.addDate(4)+"'," 
        		
        		+ "wallDamageFactor_trend='"
        		+plantFactor_Fu.get(0).get("maxWallDamageFactor_Fu").toString()+","
        		+plantFactor_Fu.get(1).get("maxWallDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(2).get("maxWallDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(3).get("maxWallDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(4).get("maxWallDamageFactor_Fu").toString()+"',"            //壁板损伤因子趋势
        		
        		+ "floorDamageFactor_trend='"
        		+plantFactor_Fu.get(0).get("floorDamageFactor_Fu").toString()+","
        		+plantFactor_Fu.get(1).get("floorDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(2).get("floorDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(3).get("floorDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(4).get("floorDamageFactor_Fu").toString()+"'"            //底板损伤因子趋势
        		
        		,Statement.RETURN_GENERATED_KEYS);
        
        pstmt.executeUpdate();                    //执行
        ResultSet rs3  = pstmt.getGeneratedKeys(); //获取结果   
        int risklistId = 0;
        if (rs3.next()) {  
        	risklistId = rs3.getInt(1);
        }
        rs3.close();
        pstmt.close();
        
        //保存壁板风险计算结果
        int i=0;
        for (Map<String, Object> WallFactor : wallFactor)  
        {  
       	  int rs4 = stmt.executeUpdate("insert into tb_riskdetail set "
       			+ "gpid="+PlantId
    			+",pid="+risklistId
    			+",part=1"
    			+",layerNO='"+WallFactor.get("layerNO").toString()+"'"
    			+",layerId='"+WallFactor.get("layerId").toString()+"'"
    			+",thicknessType='"+WallFactor.get("thicknessType").toString()+"'"
    			+",corrosionSpeed="+Double.parseDouble(WallFactor.get("corrosionSpeed").toString())+""
    			+",damageFactor="+Double.parseDouble(WallFactor.get("wallDamageFactor").toString())+"");//创建数据对象 
       	  i++;
        }       
        int re4 = 0; 
        if (   wallRiskLevel    ==  "高风险"
        	|| wallRiskLevel    ==  "中高风险"
        	|| floorRiskLevel   ==  "高风险"
        	|| floorRiskLevel   ==  "中高风险"
        ) {
            re4 = stmt.executeUpdate("update tb_risklist set isAlarm=1 where id="+risklistId);//创建数据对象
        }
        
        //创建数据对象
        if(re4>0){
        	re[0] = "计算结果成功";
        	re[1] = ""+1;
        }else{
        	re[0] = "计算结果出错";
        	re[1] = ""+0;
        }
        
        rs1.close();
        stmt.close();
        conn.close();
       
        
    } catch (Exception e) {  
        e.printStackTrace();  
    }  

//-----------------------测试专用分割线--------------------------------------------------------------------------
     
	 return re;
 }
 
 
 
 public static void main(String[] args) {  
	 Calculate calculate = new Calculate();
	
	 
	 List<Map<String,Object>> plantInfo                  = calculate.getPlantInfo(545);
     List<Map<String,Object>> wallboardInfo              = calculate.getWallboardInfo(545);
     List<Map<String,Object>> plantMech                  = calculate.getPlantMech(545);
     List<Map<String,Object>> plantTest                  = calculate.getPlantTest(545);
     List<Map<String,Object>> wallPlantCorrosion         = calculate.getWallPlantCorrosion(545);
     List<Map<String,Object>> bottomEdgePlantCorrosion   = calculate.getBottomPlantCorrosion(545,2,1);
     List<Map<String,Object>> bottomMiddlePlantCorrosion = calculate.getBottomPlantCorrosion(545,2,2);
     double useDate = 5.0;
     List<Map<String, Object>> wallFactor = new ArrayList<Map<String, Object>>(); 
     
     int i = 0;
     for (Map<String, Object> WallPlantCorrosion : wallPlantCorrosion)  
     {  
//    	 System.out.println(WallPlantCorrosion.get("layerId").toString());
    	 Map<String, Object>    wallFactorMap = new HashMap<String, Object>(); 
    	 String layerNO = WallPlantCorrosion.get("layerNO").toString();
    	 wallFactorMap.put("layerNO", layerNO);
    	 wallFactorMap.put("layerId", WallPlantCorrosion.get("layerId").toString());
    	 wallFactorMap.put("thicknessType", WallPlantCorrosion.get("thicknessType").toString());
//    	 wallFactorMap.put("corrosionSpeed", Double.parseDouble(WallPlantCorrosion.get("corrosionSpeed").toString()));
    	 wallFactor.add(wallFactorMap);
//    	 System.out.println(i);
    	 i++;
     }
     
//	 System.out.println("测试" + list);
//	 for (Map<String, Object> WallFactor : wallFactor)  
//     {  
//		 System.out.println(WallFactor.get("layerNO").toString());
//		 System.out.println(WallFactor.get("layerId").toString());
//		 System.out.println(WallFactor.get("thicknessType").toString());
//		 System.out.println(Double.parseDouble(WallFactor.get("corrosionSpeed").toString()));
//
//	 }	 
//     
     
     System.out.println(wallFactor.get(0).get("layerNO").toString());
     for (Map<String, Object> WallFactor : wallFactor)  
     {  
		 System.out.println(WallFactor.get("layerNO").toString());
		 System.out.println(WallFactor.get("layerId").toString());
		 System.out.println(WallFactor.get("thicknessType").toString());
//		 System.out.println(Double.parseDouble(WallFactor.get("corrosionSpeed").toString()));


	 }	 
 }  
	
//   获取风险计算的基本
	public List<Map<String,Object>> getPlantInfo(int plantId){
		sql = "select * from tb_plantinfo AS PlantInfo LEFT JOIN "
				+ "tb_plantwallboardinfo  AS PlantWallboardInfo "
				+ "ON PlantInfo.id = PlantWallboardInfo.pid "
				+ "where PlantInfo.id=" + plantId;//SQL语句  
	
        db1 = new DBHelper(sql);//创建DBHelper对象  
        int                i = 0;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
        try {  
            ret = db1.pst.executeQuery();//执行语句，得到结果集  
            ResultSetMetaData rsmd = ret.getMetaData() ;        
			int columns=rsmd.getColumnCount();  
            while(ret.next()){  
            Map<String, Object> map = new HashMap<String, Object>();  
            for(i = 0; i < columns; i++){  
            	switch (rsmd.getColumnTypeName(i+1))          
                {
                case "INT":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "TINYINT":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "INT NUSIGNED":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "VARCHAR":
                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
                  break;
                default :
                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
                    break;
                }
                  
            }  
            list.add(map);
        }  
       
            ret.close();  
            db1.close();//关闭连接 
        } catch (SQLException e) { 
            e.printStackTrace(); 
            
        }
        return list;  
         
	}    
	
	//获取设备的壁板数据
	public List<Map<String,Object>> getWallboardInfo(int plantId){
		sql = "select * from tb_plantwallboardinfo  where pid=" + plantId;//SQL语句  
	
        db1 = new DBHelper(sql);//创建DBHelper对象  
        int                i = 0;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
        try {  
            ret = db1.pst.executeQuery();//执行语句，得到结果集  
            ResultSetMetaData rsmd = ret.getMetaData() ;        
			int columns=rsmd.getColumnCount();  
            while(ret.next()){  
            Map<String, Object> map = new HashMap<String, Object>();  
            for(i = 0; i < columns; i++){  
            	switch (rsmd.getColumnTypeName(i+1))                     //translate the column of table type to java type then write to vector  
                {
                case "INT":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "TINYINT":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "INT NUSIGNED":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "VARCHAR":
                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
                  break;
                default :
                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
                    break;
                }
                  
            }  
            list.add(map);
            
        }  
       
            ret.close();  
            db1.close();//关闭连接 
        } catch (SQLException e) { 
            e.printStackTrace(); 
            
        }
        return list;  
	}
	    
	public List<Map<String,Object>> getPlantMech(int plantId){
		sql = "select * from tb_riskcalpara  where pid=" + plantId;//SQL语句  
	
        db1 = new DBHelper(sql);//创建DBHelper对象  
        int                i = 0;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
        try {  
            ret = db1.pst.executeQuery();//执行语句，得到结果集  
            ResultSetMetaData rsmd = ret.getMetaData() ;        
			int columns=rsmd.getColumnCount();  
            while(ret.next()){  
            Map<String, Object> map = new HashMap<String, Object>();  
            for(i = 0; i < columns; i++){  
            	switch (rsmd.getColumnTypeName(i+1))                     //translate the column of table type to java type then write to vector  
                {
                case "INT":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "TINYINT":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "INT NUSIGNED":
                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
                  break;
                case "VARCHAR":
                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
                  break;
                default :
                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
                    break;
                }
                  
            }  
            list.add(map);
        }  
       
            ret.close();  
            db1.close();//关闭连接 
        } catch (SQLException e) { 
            e.printStackTrace(); 
            
        }
        return list;  
	}

	//获取设备的检验次数和检验有效性
		public List<Map<String,Object>> getPlantTest(int plantId){
			sql = "select * from tb_planttestrecord as PlantTestRecord left join "
					+ "(select * from tb_planttestrecordwall group by pid) as PlantTestRecordWall "
					+ "on PlantTestRecord.id = PlantTestRecordWall.pid where PlantTestRecord.pid=" + plantId;//SQL语句  
		
	        db1 = new DBHelper(sql);//创建DBHelper对象  
	        int                i = 0;
	        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
	        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>(); 
	        try {  
	            ret = db1.pst.executeQuery();//执行语句，得到结果集  
	            ResultSetMetaData rsmd = ret.getMetaData() ;        
				int columns=rsmd.getColumnCount();  
	            while(ret.next()){  
	            Map<String, Object> map = new HashMap<String, Object>();  
	            for(i = 0; i < columns; i++){  
	            	switch (rsmd.getColumnTypeName(i+1))                     //translate the column of table type to java type then write to vector  
	                {
	                case "INT":
	                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
	                  break;
	                case "TINYINT":
	                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
	                  break;
	                case "INT NUSIGNED":
	                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
	                  break;
	                case "VARCHAR":
	                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
	                  break;
	                default :
	                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
	                    break;
	                }
	                  
	            }  
	            list.add(map);
	            
	        }  
	       
	        ret.close();  
	        db1.close();//关闭连接 
	        double    wallCheckTime       = 0;
	        double    bottomCheckTime     = 0;
	        double    sum                 = 0;
	        String    testMethod_wall   = null;
	        String    testMethod_bottom   = null;
	        for (Map<String, Object> map: list)
		    {
	        	testMethod_wall = map.get("testMethod").toString();
		    	  switch(testMethod_wall){
	                case "高度有效" :
	                    sum=1;
	                    break;
	                case "中高度有效" :
	                    sum=0.5;
	                    break;
	                case "中度有效" :
	                    sum=0.25;
	                    break;
	                case "低度有效" :
	                    sum=0.125;
	                    break;
	                case "无效" :
	                    sum=0;
	                    break;
	            }
		    	  wallCheckTime=wallCheckTime+sum;
	            
	            testMethod_bottom = map.get("testMethod_bottom").toString();
	            switch(testMethod_bottom){
	                case "高度有效" :
	                    sum=1;
	                    break;
	                case "中高度有效" :
	                    sum=0.5;
	                    break;
	                case "中度有效" :
	                    sum=0.25;
	                    break;
	                case "低度有效" :
	                    sum=0.125;
	                    break;
	                case "无效" :
	                    sum=0;
	                    break;
	            }
	            bottomCheckTime = bottomCheckTime + sum;
		        Map<String, Object> re = new HashMap<String, Object>(); 
		        re.put("wallCheckTime", (int) Math.ceil(wallCheckTime));
		        re.put("bottomCheckTime", (int) Math.ceil(bottomCheckTime));
		        re.put("wallCheckValidity", "D");
		        re.put("bottomCheckValidity", "D");
		        res.add(re);
		    }
	            
	            
	        } catch (SQLException e) { 
	            e.printStackTrace(); 
	            
	        }
	        return res;  
		}
		
//获取腐蚀速率相关参数		
		public List<Map<String,Object>> getWallPlantCorrosion(int plantId){
			
			sql = "select max(Mea_dt) from tb_measurethicknessrecord_wall "
					+ "where part = 1 and gpid=" + plantId;//SQL语句  
	        db1 = new DBHelper(sql);//创建DBHelper对象  
	        
	        String max_Meadt = null;
	        try {  
	            ret = db1.pst.executeQuery();//执行语句，得到结果集  
	            while(ret.next()){  
	            	max_Meadt = ret.getString(1);   
	            }  
	        ret.close();  
	        db1.close();//关闭连接    
	        } catch (SQLException e) { 
	            e.printStackTrace(); 
	            
	        }
	        
	        sql = "select * from (tb_measurethicknessrecord_wall as TMW left join tb_plantwallboardinfo "
	        		+ "as TMI on TMW.layerGpid=TMI.id) left join tb_measurethicknessrecord_wall_origin "
	        		+ "as TMO on TMW.layerPid=TMO.id where TMW.part = 1 and TMW.gpid=" + plantId + 
	        		" and TMW.Mea_dt = '"+ max_Meadt +"'";  
		
	        db1 = new DBHelper(sql);//创建DBHelper对象  
	        int                i = 0;
	        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
//	        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>(); 
	        try {  
	            ret = db1.pst.executeQuery();//执行语句，得到结果集  
	            ResultSetMetaData rsmd = ret.getMetaData() ;        
				int columns=rsmd.getColumnCount();  
	            while(ret.next()){  
	            Map<String, Object> map = new HashMap<String, Object>();  
	            for(i = 0; i < columns; i++){  
	            	switch (rsmd.getColumnTypeName(i+1))                     //translate the column of table type to java type then write to vector  
	                {
	                case "INT":
	                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
	                  break;
	                case "TINYINT":
	                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
	                  break;
	                case "INT NUSIGNED":
	                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
	                  break;
	                case "VARCHAR":
	                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
	                  break;
	                default :
	                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
	                    break;
	                }
	                  
	            }  
	            list.add(map);
	        }  
	       
	            ret.close();  
	            db1.close();//关闭连接 
	        } catch (SQLException e) { 
	            e.printStackTrace(); 
	            
	        }
	        
	        return list;  
		}
		
		//获取底板腐蚀速率相关参数		
				public List<Map<String,Object>> getBottomPlantCorrosion(int plantId, int part, int layerNO){
					
					sql = "select max(Mea_dt) from tb_measurethicknessrecord_wall "
							+ "where part = "+part+" and gpid=" + plantId +
							" and layerNO =" + layerNO;//SQL语句  
			        db1 = new DBHelper(sql);//创建DBHelper对象  
			        
			        String max_Meadt = null;
			        try {  
			            ret = db1.pst.executeQuery();//执行语句，得到结果集  
			            while(ret.next()){  
			            	max_Meadt = ret.getString(1);   
			            }  
			        ret.close();  
			        db1.close();//关闭连接    
			        } catch (SQLException e) { 
			            e.printStackTrace(); 
			            
			        }
			        
			        sql = "select * from (tb_measurethicknessrecord_wall as TMW left join tb_plantwallboardinfo "
			        		+ "as TMI on TMW.layerGpid=TMI.id) left join tb_measurethicknessrecord_wall_origin "
			        		+ "as TMO on TMW.layerPid=TMO.id where TMW.part = "+part+" and TMW.gpid=" + plantId + 
			        		" and TMW.Mea_dt = '"+ max_Meadt +"'"+ "and TMW.layerNO =" + layerNO;  
				
			        db1 = new DBHelper(sql);//创建DBHelper对象  
			        int                i = 0;
			        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
//			        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>(); 
			        try {  
			            ret = db1.pst.executeQuery();//执行语句，得到结果集  
			            ResultSetMetaData rsmd = ret.getMetaData() ;        
						int columns=rsmd.getColumnCount();  
			            while(ret.next()){  
			            Map<String, Object> map = new HashMap<String, Object>();  
			            for(i = 0; i < columns; i++){  
			            	switch (rsmd.getColumnTypeName(i+1))                     //translate the column of table type to java type then write to vector  
			                {
			                case "INT":
			                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
			                  break;
			                case "TINYINT":
			                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
			                  break;
			                case "INT NUSIGNED":
			                	map.put(rsmd.getColumnName(i + 1), ret.getInt(i+1));
			                  break;
			                case "VARCHAR":
			                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
			                  break;
			                default :
			                	map.put(rsmd.getColumnName(i + 1), ret.getString(i+1));
			                    break;
			                }
			                  
			            }  
			            list.add(map);
			        }  
			       
			            ret.close();  
			            db1.close();//关闭连接 
			        } catch (SQLException e) { 
			            e.printStackTrace(); 
			            
			        }
			        
			        return list;  
				}

	//计算失效可能性：Fg 平局失效可能性   Fm 管理评级按系数   Fe 损伤因子
	//三者的乘积等于失效可能性
	public double getFailurePro(double Fg,double Fm,double Fe){
		double failPro = Fg*Fm*Fe;
		return failPro;
	}
	
	/*计算平均失效概率
	 * 1、part代表壁板或者底板 0代表底板 1代表壁板
	 * 2、break是泄漏孔尺寸
	 * */
	public double getAverageFailurePro(int part,double breakSize){
	    int    breakSizeIndex = 0;
	    double res;
	    if(breakSize >= 0 && breakSize < 6){
	    	breakSizeIndex = 0;
	    }else if(breakSize>=6 && breakSize<25){
	    	 breakSizeIndex=1;
	    }else if(breakSize>=25 && breakSize<100){
	    	breakSizeIndex=2;
	    }else if(breakSize>=100){
	    	 breakSizeIndex=3;
	    }
	  //概率不可能为0，这里的标准应该有问题，将概率为0的改为0.00001
	  //该数组根据GB22610的标准
		double[][] averageFailProArr = {
				{0.00072, 0.00001, 0.00001, 0.00002},
				{0.00007, 0.000025, 0.000005, 0.0000007}
		};
		res = averageFailProArr[part][breakSizeIndex];
		return res;
	}
	
//--------------------------------------------------------------------------------------------
//计算损伤因子
	/* 1、corrosionType           腐蚀类型，局部腐蚀还是均匀腐蚀  0表示均匀腐蚀   1表示局部腐蚀
	 * 2、reductionDamageFaction  减薄损伤因子
	 * 3、outDamageFaction        外部损伤因子
	 * 4、SCCDamageFaction        应力腐蚀开裂损伤因子
	 * 5、brittleFractureFaction  脆性断裂损伤因子
	 * */
	public double getDamageFaction(
			int    corrosionType,
			double reductionDamageFactor,
			double outDamageFactor,
			double SCCFactor,
			double brittleFractureFactor
			){
		 double damageFactor = 1;
		 if(corrosionType==0){
	            damageFactor = reductionDamageFactor + outDamageFactor + SCCFactor + brittleFractureFactor;
	        }else if(corrosionType==1){
	            damageFactor = Math.max(reductionDamageFactor,outDamageFactor) + SCCFactor + brittleFractureFactor;   
	        }
	     return damageFactor;  
	}
	
//计算减薄损伤因子
	/* 1、part                       //表示壁板或者底板 0代表底板   1代表壁板
	 * 2、corrosionSpeed             //腐蚀速率
	 * 3、useDate                    //使用年限
	 * 4、thickness                  //厚度
	 * 5、checkTime                  //检验次数
	 * 6、checkValidity             //检验有效性
	 * 7、linkType                   //连接形式
	 * 8、isMaintenanceAsRequired    //是否按照要求进行维护  0代表否  1代表是
	 * 9、tankFoundationSettlement   //基础沉降评价
	 * */
  public double getReductionDamageFactor(
			int     part,             
			double  corrosionSpeed,
			double  useDate,
			double  thickness,
			int     checkTime,
			String  checkValidity,
			String  linkType,
			int     isMaintenanceAsRequired,
			int     tankFoundationSettlement
			){
       
		double   severity                 = 0;             //严重程度指数
		double   reductionDamageFactor    = 1;             //减薄损伤因子
		
		String[] checkConditionIndexArr   = {              //检验次数和检验有效性检索表，如“1A”、“4D”
				"0", "1A", "1B", "1C", "1D", 
				"2A", "2B", "2C", "2D", 
				"3A", "3B", "3C", "3D", 
				"4A", "4B", "4C", "4D", 
				"5A", "5B", "5C", "5D",
				"6A", "6B", "6C", "6D"	
		};
		
		double[] severityIndexArr         = {              //严重程度指数检索表
				0.02, 0.04, 0.06, 0.08, 0.10, 0.12, 
				0.14, 0.16, 0.18, 0.20, 0.25, 0.30, 
				0.35, 0.40, 0.45, 0.50, 0.55, 0.60, 
				0.65
		};
		
		 //    壁板减薄次因子的标准，根据GB22610.4表C.7
        int[][] reductionWallDamageFactorTable = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {6, 5, 3, 2, 1, 4, 2, 1, 1, 3, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1},
            {20, 17, 10, 6, 1, 13, 6, 1, 1, 10, 3, 1, 1, 7, 2, 1, 1, 5, 1, 1, 1, 4, 1, 1, 1},
            {90, 70, 50, 20, 3, 50, 20, 4, 1, 40, 10, 1, 1, 30, 5, 1, 1, 20, 2, 1, 1, 14, 1, 1, 1},
            {250, 200, 130, 70, 7, 170, 70, 10, 1, 130, 35, 3, 1, 100, 15, 1, 1, 70, 7, 1, 1, 50, 3, 1, 1},
            {400, 300, 210, 110, 15, 290, 120, 20, 1, 260, 60, 5, 1, 180, 20, 2, 1, 120, 10, 1, 1, 100, 6, 1, 1},
            {520, 450, 290, 150, 20, 350, 170, 30, 20, 240, 80, 6, 1, 200, 30, 2, 1, 150, 15, 2, 1, 120, 7, 1, 1},
            {650, 550, 400, 200, 30, 400, 200, 40, 4, 320, 110, 9, 2, 240, 50, 4, 2, 180, 25, 3, 2, 150, 10, 2, 2},
            {750, 650, 550, 300, 80, 600, 300, 80, 10, 540, 150, 20, 5, 440, 90, 10, 4, 350, 70, 6, 4, 280, 40, 5, 4},
            {900, 800, 700, 400, 130, 700, 400, 120, 30, 600, 200, 50, 10, 500, 140, 20, 8, 400, 110, 10, 8, 350, 90, 9, 8},
            {1050, 900, 810, 500, 200, 800, 500, 160, 40, 700, 270, 60, 20, 600, 200, 30, 15, 500, 160, 20, 15, 400, 130, 20, 15},
            {1200, 1100, 970, 600, 270, 1000, 600, 200, 60, 900, 360, 80, 40, 800, 270, 50, 40, 700, 210, 40, 40, 600, 180, 40, 40},
            {1350, 1200, 1130, 700, 350, 1100, 750, 300, 100, 1000, 500, 130, 90, 900, 350, 100, 90, 800, 260, 90, 90, 700, 240, 90, 90},
            {1500, 1400, 1250, 850, 500, 1300, 900, 400, 230, 1200, 650, 250, 210, 1000, 450, 220, 210, 900, 360, 210, 210, 800, 300, 210, 210},
            {1900, 1700, 1400, 1000, 700, 1600, 1105, 670, 530, 1300, 880, 550, 500, 1200, 700, 530, 500, 1100, 640, 500, 500, 1000, 600, 500, 500}
        };
//      底板减薄次因子的标准，根据GB22610.4表C.7
        int[][] reductionFloorDamageFactorTable = {
            {4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {14, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {32, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {56, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {87, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {125, 5, 3, 2, 1, 4, 2, 1, 1, 3, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1},
            {170, 17, 10, 6, 1, 13, 6, 1, 1, 10, 3, 1, 1, 7, 2, 1, 1, 5, 1, 1, 1, 4, 1, 1, 1},
            {222, 70, 50, 20, 3, 50, 20, 4, 1, 40, 10, 1, 1, 30, 5, 1, 1, 20, 2, 1, 1, 14, 1, 1, 1},
            {281, 200, 130, 70, 7, 170, 70, 10, 1, 130, 35, 3, 1, 100, 15, 1, 1, 70, 7, 1, 1, 50, 3, 1, 1},
            {347, 300, 210, 110, 15, 290, 120, 20, 1, 260, 60, 5, 1, 180, 20, 2, 1, 120, 10, 1, 1, 100, 6, 1, 1},
            {420, 450, 290, 150, 20, 350, 170, 30, 20, 240, 80, 6, 1, 200, 30, 2, 1, 150, 15, 2, 1, 120, 7, 1, 1},
            {500, 550, 400, 200, 30, 400, 200, 40, 4, 320, 110, 9, 2, 240, 50, 4, 2, 180, 25, 3, 2, 150, 10, 2, 2},
            {587, 650, 550, 300, 80, 600, 300, 80, 10, 540, 150, 20, 5, 440, 90, 10, 4, 350, 70, 6, 4, 280, 40, 5, 4},
            {681, 800, 700, 400, 130, 700, 400, 120, 30, 600, 200, 50, 10, 500, 140, 20, 8, 400, 110, 10, 8, 350, 90, 9, 8},
            {782, 900, 810, 500, 200, 800, 500, 160, 40, 700, 270, 60, 20, 600, 200, 30, 15, 500, 160, 20, 15, 400, 130, 20, 15},
            {890, 1100, 970, 600, 270, 1000, 600, 200, 60, 900, 360, 80, 40, 800, 270, 50, 40, 700, 210, 40, 40, 600, 180, 40, 40},
            {1005, 1200, 1130, 700, 350, 1100, 750, 300, 100, 1000, 500, 130, 90, 900, 350, 100, 90, 800, 260, 90, 90, 700, 240, 90, 90},
            {1126, 1209, 1163, 1118, 1098, 1300, 900, 400, 230, 1200, 650, 250, 210, 1000, 450, 220, 210, 900, 360, 210, 210, 800, 300, 210, 210},
            {1390, 1390, 1390, 1390, 1390, 1600, 1105, 670, 530, 1300, 880, 550, 500, 1200, 700, 530, 500, 1100, 640, 500, 500, 1000, 600, 500, 500}
        };

//		计算严重程度指数
		severity = (double)(Math.round(thickness * corrosionSpeed / useDate*100)/100.0);
//      计算检验次数和检验有效性的
		String   checkCondition           = "0";  //检验次数和检验有效性索引值，如“1A”、“4D”
		if(checkTime == 0){
			checkCondition = "0";
		}else if(checkTime > 0 && checkTime <= 6){
			checkCondition = checkTime+checkValidity;
		}else if(checkTime > 6){
			checkCondition = "6"+checkValidity;
		}
//	    计算严重程度指数索引值
		int severityIndex  =  0;
		for (int i = 1; i < severityIndexArr.length; i++) { 			 
			 double a             = Math.abs(severityIndexArr[0] - severity);
			 double b             = Math.abs(severityIndexArr[i] - severity);
			 if(b<a){
			    severityIndex  =  i;
				a              = Math.abs(severityIndexArr[i] - severity);
			 }
		}
//	    计算检验次数和检验有效性索引值
		int checkConditionIndex  =  0;	
		for (int i = 0; i < checkConditionIndexArr.length; i++) {  
            if (checkConditionIndexArr[i].equals(checkCondition)) {  
            	checkConditionIndex  =  i;
            }  
        }
//    计算减薄次因子
		 if(part==1){
			 reductionDamageFactor = reductionWallDamageFactorTable[severityIndex][checkConditionIndex];
	        }else if(part==0){
	         reductionDamageFactor = reductionFloorDamageFactorTable[severityIndex][checkConditionIndex];
	        }
		 //焊接系数的影响
		 int fWD = 1;
		    if(linkType.equals("焊接")){
	            fWD=1;
	        }else{
	            fWD=10;
	        }
	     //储罐维护修正系数 1代表是，0代表否，是否按照要求维护
		 double fAM = 1;
	        if(isMaintenanceAsRequired==1){
	            fAM=1;
	        }else{
	            fAM=5;
	        }
	     //基础沉降评价等级的影响
	     double fSM = 1;
	        switch(tankFoundationSettlement){
	            case 1:  fSM=2;break;
	            case 2:  fSM=1;break;
	            case 3:  fSM=1.5;break;
	            case 4:  fSM=1;break;
	            default:  fSM=1; break;
	        }
	    reductionDamageFactor=reductionDamageFactor*fWD*fAM*fSM;
	        
	 return reductionDamageFactor;	       
	}
	
//------------------------------SCC应力腐蚀开裂损伤因子--------------------------------------------------------------------
/* 1、碱开裂  
 * 2、胺开裂  
 * 3、硫化物应力腐蚀开裂（SSCC） 
 * 4、碳酸盐腐蚀开裂  
 * 5、连多硫酸开裂（PTA）
 * 6、氯化物应力腐蚀开裂（CISCC） 
 * 7、氢应力开裂（HSC-HF、HIC/SOHIC_HF）
 * */
  public double getSCCDamageFactor(
		double operatingTemp,                  //操作温度
		double designTemp,                     //设计温度
		int    SCCMechId,                      //SCC损伤机理
		int    checkTime,                      //检验次数
		String checkValidity,                  //检验有效性
		int    severityPara,                   //计算外部损伤因子的时候不需要计算敏感性高低，直接输入严重程度指数
		int    isMediumWater,                  //是否介质含水
		double SCCWaterH2S,                    //水中的硫化氢含量
		double SCCWaterpH,                     //水中pH值
		int    isHeatTreatAfterWeld,            //是否焊后热处理
		double SCCBHardness,                   //最大布氏硬度
		double clConcentration,                //水中的氯离子含量
		
		int    isHeatTreacing,                 //是否伴热
		double NaOHConcentration,               //NaOH浓度
		int    isSteamBlowing,                 //是否蒸汽吹扫
		int    isStressRelief,                  //是否进行应力消除
		int    SCCHeatHistory,                  //热历史
		int    SCCisShutdownProtect,            //是否停机保护
		String surrounding,                     //环境含有物
		double SCCSteelSulfur,                  //钢板中硫含量
		double SCCWaterCarbonateConcentration   //碳酸盐浓度
		   ){
//	   敏感性无为0.依次为低、中、高，对应1/2/3
	   int      sensitive = 0;
	   int      severity  = 0;
	   int[][]  severityArr = {
			    {1,1,1,1,1,1,1},//敏感性为无
	            {50,10,10,1,1,20,20},//敏感性为低
	            {500,100,100,10,10,500,500},//敏感性为中
	            {5000,1000,1000,1,1,5000,5000}//敏感性为高
	   };
	    switch(SCCMechId){
//	    已知严重程度指数
	    case 0:
	        severity = severityPara;
	    	break;
//	   碱开裂敏感性高低
	    case 1:
	    	/*涉及的参数
	    	 * 1、NaOH浓度
	    	 * 2、是否进行应力消除
	    	 * 3、操作运行温度
	    	 * 4、是否蒸汽吹扫
	    	 * 5、是否伴热
	    	 * */
	    	if(isStressRelief == 1){
	    		sensitive=0;
	    	}else{
	    		 if(78-0.6*NaOHConcentration < operatingTemp){
//                   NaOH浓度是否小于5%
                    if(NaOHConcentration<0.05){
//                       是否伴热
                        if(isHeatTreacing == 1){
                            sensitive=2;
                        }else{
//                           是否蒸汽吹扫
                            if(isSteamBlowing == 1){
                                sensitive = 1;
                            }else{
                                sensitive = 0;
                            }
                        }
                    }else{
//                       是否伴热
                        if(isHeatTreacing == 1){
                            sensitive = 3;
                        }else{
//                           是否蒸汽吹扫
                            if(isSteamBlowing == 1){
                                sensitive = 2;
                            }else{
                                sensitive = 0;
                            }
                        }
                    }
                }else{
                    if(NaOHConcentration < 0.05){
                        sensitive = 2;
                    }else{
                        sensitive = 3;
                    }
                }	
	         severity = severityArr[sensitive][0];
	    	}
	    	break;
	    case 2:
/*胺开裂应力腐蚀开裂敏感性筛选
涉及的参数
1、胺的类型
2、胺液的成分  1代表新鲜胺未吸收H2S或CO2  2代表贫胺含低浓度H2S或CO2  3代表富胺含有高浓度H2S或CO2
3、最高工艺温度
4、是否伴热
5、蒸汽吹扫
6、是否消除应力
*/
//	    	是否应力消除
	    if(isStressRelief == 1){
//              敏感性无为0.依次为高、中、低，对应1/2/3
              sensitive=0;
          }else{
//              环境参数，贫胺、MEA或DIPA，DEA
              if(surrounding.equals("贫胺")){
                  if(surrounding.equals("MEA") || surrounding.equals("DIPA")){
                      if(operatingTemp > 82){
                          sensitive = 3;
                      }else{
                          if( operatingTemp > 32 && operatingTemp < 82){
                              sensitive = 2;
                          }else{
//                                是否伴热
                              if(isHeatTreacing == 1){
                                   sensitive = 2;
                              }else{
                                  if(isSteamBlowing == 0){
                                      sensitive = 2;
                                  }else{
                                      sensitive = 1;
                                  }
                              }
                          }
                      }
                  }else{
                      if(surrounding.equals("DEA")){
                          if(operatingTemp > 82){
                              sensitive = 2;
                          }else{
                              if(operatingTemp > 60 && operatingTemp < 82){
                                  sensitive = 1;
                              }else{
                                  if(isHeatTreacing == 1){
                                      sensitive = 1;
                                  }else{
                                      if(isSteamBlowing == 1){
                                          sensitive = 1;
                                      }else{
                                          sensitive = 0;
                                      }
                                  }
                              }
                          }
                      }else{
                          if(operatingTemp > 82){
                              sensitive = 1;
                          }else{
                              if(isHeatTreacing == 1){
                                  sensitive = 1;
                              }else{
                                  if(isSteamBlowing == 1){
                                      sensitive = 1;
                                  }else{
                                      sensitive = 0;
                                  }
                              }
                          }
                      }
                  }
              }else{
                  sensitive = 0;
              }

          }
          severity = severityArr[sensitive][1];	
	    	break;
	    case 3:
//	    	硫化物应力腐蚀开裂敏感性筛选
	    	/*涉及的参数：
	    	 * 1、是否存在水
	    	 * 2、水中的硫化氢含量
	    	 * 3、水中的pH值
	    	 * 4、是否存在氰化物
	    	 * 5、最大布氏硬度
	    	 * 6、是否焊后热处理
	    	 * */
	    	if(isMediumWater == 1){
//              水中的H2S含量横坐标索引
	    		int waterH2SConcentrationIndex = 0;
	    		if(SCCWaterH2S < 0.00005){
	    			 waterH2SConcentrationIndex = 0;
	    		}else if(SCCWaterH2S >= 0.00005 && SCCWaterH2S < 0.001){
	    			 waterH2SConcentrationIndex = 1;
	    		}else if(SCCWaterH2S >=0.001 && SCCWaterH2S < 0.01){
	    			 waterH2SConcentrationIndex=2;
	    		}else if(SCCWaterH2S >= 0.01){
	    			 waterH2SConcentrationIndex=3;
	    		}
//              水中的PH值纵坐标索引
	    		int SCCWaterpHIndex = 0;
	    		if(SCCWaterpH < 5.5){
	    			 SCCWaterpHIndex = 0;
	    		}else if(SCCWaterpH >= 5.5 && SCCWaterpH < 7.5){
	    			 SCCWaterpHIndex=1;
	    		}else if(SCCWaterpH >= 7.5 && SCCWaterpH < 8.3){
	    			 SCCWaterpHIndex=2;
	    		}else if(SCCWaterpH >= 8.3 && SCCWaterpH < 9.0){
	    			 SCCWaterpHIndex=3;
	    		}else if(SCCWaterpH >= 9.0){
	    			 SCCWaterpHIndex=4;
	    		}
             
              //环境苛刻度的标准表格 表22610.4 表D.9
              int[][] environmentalSeverityArr={
//                  1 代表低  2 代表中  3 代表高
                  {1,2,3,3},
                  {1,1,1,2},
                  {1,2,2,2},
                  {1,2,2,3},
                  {1,2,3,3}
              };
              int environmentalSeverity=environmentalSeverityArr[SCCWaterpHIndex][waterH2SConcentrationIndex];
              environmentalSeverity= environmentalSeverity*1-1;
//              sscc敏感性标准表格的 表22610.4 表D.10
              int[][] sensitiveArr={
                  {2,1,1,1,0,0},
                  {3,2,2,1,1,1},
                  {3,3,3,2,2,1}
              };
              int SCCBHardnessIndex=0;
              if(isHeatTreatAfterWeld == 1){
            	  
            	 if(SCCBHardness<200){
            		 SCCBHardnessIndex=0;
            	 }else if(SCCBHardness >= 200 && SCCBHardness < 237){
            		 SCCBHardnessIndex=1;
            	 }else if(SCCBHardness >= 237){
            		 SCCBHardnessIndex=2;
            	 }
              }else if(isHeatTreatAfterWeld == 0){
            	  if(SCCBHardness < 200){
            		  SCCBHardnessIndex = 3;
            	  }else if(SCCBHardness >=200 && SCCBHardness < 237){
            		  SCCBHardnessIndex = 4;
            	  }else if(SCCBHardness >= 237){
            		  SCCBHardnessIndex=5;
            	  }
              }
//              敏感度1表示低，2表示中，3表示高
               sensitive = sensitiveArr[environmentalSeverity][SCCBHardnessIndex];
          }else{
              sensitive = 0;
          }
	      int sensitiveSSCC=sensitive;
//-----------------------------------------湿硫化氢环境下氢致开裂和应力导向氢致开裂(HIC/SOHIC-H2S)---------------------------------------------
//涉及参数：
//1、是否存在水
//2、水中是否存在H2S
//3、水中pH值
//4、是否有氰化物
//5、钢板中的硫化物
//6、钢产品形式   1、轧制钢板焊接 /2、无缝钢管制造

           if(isMediumWater == 1){
//              水中的H2S含量横坐标索引
        	   int waterH2SConcentrationIndex = 0;
        	   if(SCCWaterH2S < 0.00005){
        		   waterH2SConcentrationIndex = 0;
        	   }else if(SCCWaterH2S >= 0.00005 && SCCWaterH2S < 0.001){
        		   waterH2SConcentrationIndex = 1;
        	   }else if(SCCWaterH2S >= 0.001 && SCCWaterH2S < 0.01){
        		   waterH2SConcentrationIndex = 2;
        	   }else if(SCCWaterH2S >= 0.01){
        		   waterH2SConcentrationIndex = 3;
        	   }
//               水中的PH值纵坐标索引
        	   int SCCWaterpHIndex = 0;
        	   if(SCCWaterpH<5.5){
        		   SCCWaterpHIndex = 0;
        	   }else if(SCCWaterpH >= 5.5 && SCCWaterpH < 7.5){
        		   SCCWaterpHIndex = 1;
        	   }else if(SCCWaterpH >= 7.5 && SCCWaterpH < 8.3){
        		   SCCWaterpHIndex = 2;
        	   }else if(SCCWaterpH >= 8.3 && SCCWaterpH < 9.0){
        		   SCCWaterpHIndex = 3;
        	   }else if(SCCWaterpH >= 9.0){
        		   SCCWaterpHIndex = 4;
        	   }
//              环境苛刻度的标准表格 根据22610.4 表D.12
               int[][] environmentalSeverityArr = {
//                   1 代表低  2 代表中  3 代表高
                   {1,2,3,3},
                   {1,1,1,2},
                   {1,2,2,2},
                   {1,2,2,3},
                   {1,2,3,3}
               };
//              求出环境苛刻度
               int environmentalSeverity = environmentalSeverityArr[SCCWaterpHIndex][waterH2SConcentrationIndex];
//              纵坐标环境苛刻度索引
                environmentalSeverity = environmentalSeverity*1-1;
//              横坐标硫含量和是否焊后热处理索引
                int SCCSteelSulfurIndex=1;
               if(isHeatTreatAfterWeld == 1){
//                  钢板中的硫含量 (已经焊后热处理的)
            	   if(SCCSteelSulfur > 0.0001){
            		   SCCSteelSulfurIndex = 1;
            	   }else if(SCCSteelSulfur >= 0.00002 && SCCSteelSulfur <= 0.0001){
            		   SCCSteelSulfurIndex = 3;
            	   }else if(SCCSteelSulfur < 0.00002){
            		   SCCSteelSulfurIndex = 5;
            	   }
               }else if(isHeatTreatAfterWeld == 0){
            	   if(SCCSteelSulfur > 0.0001){
            		   SCCSteelSulfurIndex = 0;
            	   }else if(SCCSteelSulfur >= 0.00002 && SCCSteelSulfur <= 0.0001){
            		   SCCSteelSulfurIndex = 2;
            	   }else if(SCCSteelSulfur < 0.00002){
            		   SCCSteelSulfurIndex = 4;
            	   }
               }
//               SSCC敏感性标准表格的 表22610.4 表D.13
               int[][] sensitiveArr = {
                   {2,1,1,1,0,0},
                   {3,2,2,1,1,1},
                   {3,3,3,2,2,1}
               };
               sensitive = sensitiveArr[environmentalSeverity][SCCSteelSulfurIndex];
           }else{
               sensitive = 0;
           }
           int sensitiveHIC = sensitive;
           sensitive = Math.max(sensitiveSSCC,sensitiveHIC);
           //最终输出的结果是严重程度指数
           severity = severityArr[sensitive][3];
	    	break;
	    case 4:
//------------------------------------碳酸盐腐蚀开裂---------------------------------------
/*涉及的参数
 * 1、是否应力消除
 * 2、碳酸盐浓度
 * 3、水中pH值
 */          
	    	if(isStressRelief == 1){
                sensitive = 0;
            }else if(isStressRelief == 0) {
                if (isMediumWater == 1) {
                    if (SCCWaterH2S > 0.00005) {
                        //碳酸盐浓度
                    	int SCCWaterCarbonateConcentrationIndex = 0;
                    	if(SCCWaterCarbonateConcentration < 0.0001){
                    		SCCWaterCarbonateConcentrationIndex = 0;
                    	}else if(SCCWaterCarbonateConcentration >= 0.0001 && SCCWaterCarbonateConcentration < 0.0005){
                    		SCCWaterCarbonateConcentrationIndex = 1;
                    	}else if(SCCWaterCarbonateConcentration >= 0.0005 && SCCWaterCarbonateConcentration < 0.001){
                    		SCCWaterCarbonateConcentrationIndex = 2;
                    	}else if(SCCWaterCarbonateConcentration >= 0.001){
                    		SCCWaterCarbonateConcentrationIndex = 3;
                    	}
                        //水中PH值
                    	int SCCWaterpHIndex = 0;
                    	if(SCCWaterpH >= 7.8 && SCCWaterpH < 8.3){
                    		SCCWaterpHIndex = 0;
                    	}else if(SCCWaterpH >= 8.3 && SCCWaterpH < 8.9){
                    		SCCWaterpHIndex = 1;
                    	}else if(SCCWaterpH < 8.9){
                    		SCCWaterpHIndex = 2;
                    	}
//                        碳酸盐敏感性标准表格，22610.4 表D.15
                        int[][] sensitiveCarbonateArr = {
                            {1, 1, 1, 2},
                            {1, 1, 2, 3},
                            {1, 2, 3, 3}
                        };
                        sensitive = sensitiveCarbonateArr[SCCWaterpHIndex][SCCWaterCarbonateConcentrationIndex];
                    } else {
                        sensitive = 0;
                    }
                } else {
                    sensitive = 0;
                }
            }
            severity = severityArr[sensitive][2];
	    	break;
	    case 5:
// -----------------连多硫酸开裂（PTA）-----------------------------
//      涉及参数：
//      1、材料类别
//      2、热历史  1代表固溶退火 2、焊前稳定处理  3、焊后热处理
//      3、最高运行温度
//      4、是否采用停工保护
//      最高运行温度采用设计温度
      if(designTemp < 427){
          switch(SCCHeatHistory){
              case 1:
                  sensitive = 2;
                  break;
              case 2:
                  sensitive = 1;
                  break;
              case 3:
                  sensitive = 1;
                  break;
              default:
                  sensitive = 1;
                  break;
          }
      }else{
          switch(SCCHeatHistory){
              case 1:
                  sensitive = 1;
                  break;
              case 2:
                  sensitive = 1;
                  break;
              case 3:
                  sensitive = 1;
                  break;
              default:
                  sensitive = 1;
                  break;
          }
      }
      if(SCCisShutdownProtect == 1){
          sensitive = sensitive*1-1;
      }
      severity = severityArr[sensitive][6];
	    	break;
	    case 6:
// --------氯化物应力腐蚀开裂（CISCC）----------------------------------------------
//涉及参数：
//1、工艺水的CL-浓度
//2、运行温度
//3、工艺水的pH值
	    	int Xindex = 0;
	    	if(clConcentration <= 10 && clConcentration > 1){
	    		Xindex = 0;
	    	}else if(clConcentration <= 100 && clConcentration > 10){
	    		Xindex = 1;
	    	}else if(clConcentration <= 1000 && clConcentration > 100){
	    		Xindex = 2;
	    	}else if(clConcentration > 1000){
	    		Xindex = 3;
	    	}
          if(SCCWaterpH <= 10){
        	  int Yindex = 0;
        	  if(designTemp <= 66 && designTemp > 38){
        		  Yindex = 0;
        	  }else if(designTemp <= 93 && designTemp > 66){
        		  Yindex = 1;
        	  }else if(designTemp <= 149 && designTemp > 94){
        		  Yindex = 2;
        	  }
              int[][] Arr = {
                  {1,2,2,3},
                  {2,2,3,3},
                  {2,3,3,3}
              };
              sensitive = Arr[Xindex][Yindex];
          }else{
        	  int Yindex = 0;
        	  if(designTemp <= 93){
        		  Yindex = 0;
        	  }else if(designTemp <= 149 && designTemp > 93){
        		  Yindex = 1;
        	  }
              int[][] Arr = {
                  {1,1,1,1},
                  {2,1,1,2}
              };
              sensitive = Arr[Xindex][Yindex];
          }
          severity = severityArr[sensitive][5];
	    	break;
	    case 7:
	    	sensitive = 1;
            severity = severityArr[sensitive][4];
            break;	
	    }
	    //--------------------------------确定SCC应力腐蚀开裂因子-------------------------------------
	 // 应力腐蚀开裂因子根据标准22610.4 表D.5
	  double[][] SCCArr={
	      {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	      {10,8,3,1,1,6,2,1,1,4,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1},
	      {50,40,17,5,3,30,10,2,1,20,5,1,1,10,1,1,1,5,1,1,1,1,1,1,1},
	      {100,80,33,10,5,60,20,4,1,40,10,2,1,20,2,1,1,10,2,1,1,5,1,1,1},
	      {500,400,170,50,25,300,100,20,5,200,50,8,1,100,12,2,1,50,10,1,1,25,5,1,1},
	      {1000,800,330,100,50,600,200,40,10,400,100,16,2,200,50,5,1,100,25,2,1,50,10,1,1},
	      {5000,4000,1670,500,250,3000,1000,250,50,2000,500,80,10,1000,250,25,2,500,125,5,1,250,50,2,1}
	  };
	  int[] SCCTableYIndexArr={1,10,50,100,500,1000,5000};
	  String   checkCondition           = "0";  //检验次数和检验有效性索引值，如“1A”、“4D”
	  if(checkTime == 0){
			checkCondition = "0";
		}else if(checkTime > 0 && checkTime <= 6){
			checkCondition = checkTime+checkValidity;
		}else if(checkTime > 6){
			checkCondition = "6"+checkValidity;
		}
	  String[] checkConditionIndexArr = {
			  "0", "1A", "1B", "1C", "1D", "2A", 
			  "2B", "2C", "2D", "3A", "3B", "3C", 
			  "3D", "4A", "4B", "4C", "4D", "5A", 
			  "5B", "5C", "5D", "6A", "6B", "6C", 
			  "6D"};
//	    计算检验次数和检验有效性索引值
	  int checkConditionIndex = 0;
		for (int i = 0; i < checkConditionIndexArr.length; i++) {  
          if (checkConditionIndexArr[i].equals(checkCondition)) {  
          	 checkConditionIndex  =  i;
          }  
      }
//	    计算严重性程度索引值
	  int severityIndex = 0;
		for (int i = 0; i < SCCTableYIndexArr.length; i++) {  
          if (SCCTableYIndexArr[i] == severity) {  
        	  severityIndex  =  i;
          }  
      }
	  
	  double SCCFactor = SCCArr[severityIndex][checkConditionIndex];
	  return SCCFactor;
   }	
  
//--------------------------------------外部损伤因子计算模块---------------------------------------------------------
  public double getOutDamageFactor(
		double  operatingTemp,               //操作温度
		double  designTemp,                  //设计温度
		int     part,                        //储罐部位，1代表壁板，0代表底板
		double  thickness,                   //底板或壁板原始厚度
		
		int     pipeComplexity,               //管道复杂度
		int     isKeepWarmHasCL,              //是否保温层含氯
		int     isPipeSupport,                //是否管支架补偿
		int     isInterfaceCompensation,      //是否界面补偿
		
		
		int     outDamageMechanismId,        //外部损伤机理
		double  useDate,                     //使用时间
		int     isKeepWarm,                  //是否有保温层
		int     KeepWarmStatus,              //保温层质量
		int     coatingStatus,               //涂层质量
		
		double  coatingUseDate,              //涂层使用使用时间
		String  linkType,                    //链接形式
		int     SCCMechanismId,              //SCC损伤机理
		int     checkTime,                   //检验次数
		String  checkValidity,               //检验有效性
		
		int     geographyEnvironment,        //设备环境
		int     isMaintenanceAsRequired,     //是否按照要求进行维护
		int     tankFoundationSettlement,     //基础沉降评价
		int    isMediumWater,                  //是否介质含水
		double SCCWaterH2S,                    //水中的硫化氢含量
		double SCCWaterpH,                     //水中pH值
		int    isHeatTreatAfterWeld,            //是否焊后热处理
		double SCCBHardness,                   //最大布氏硬度
		double clConcentration,                //水中的氯离子含量
		
		int    isHeatTreacing,                 //是否伴热
		double NaOHConcentration,               //NaOH浓度
		int    isSteamBlowing,                 //是否蒸汽吹扫
		int    isStressRelief,                  //是否进行应力消除
		int    SCCHeatHistory,                  //热历史
		int    SCCisShutdownProtect,            //是否停机保护
		String surrounding,                     //环境含有物
		double SCCSteelSulfur,                  //钢板中硫含量
		double SCCWaterCarbonateConcentration   //碳酸盐浓度

  ){
	double outDamageFactor = 1;                        //初始化外部损伤因子
	Calculate calculate = new Calculate(); 
	
	switch(outDamageMechanismId){
	//------------------------------------------------碳钢和低合金钢的外部腐蚀机理外部损伤因子-----------------------------------------------------
		case 1: //1代表"碳钢和低合金钢的外部损伤"
	//	     纵坐标索引（运行温度）
			int outCarbonSteelCorrosionSpeedTableYIndex = 0;
		if(operatingTemp < -12){
			outCarbonSteelCorrosionSpeedTableYIndex = 0;
		}else if(operatingTemp >= -12 && operatingTemp < 15){
			outCarbonSteelCorrosionSpeedTableYIndex=1;
		}else if(operatingTemp >= 15 && operatingTemp < 49){
			outCarbonSteelCorrosionSpeedTableYIndex=2;
		}else if(operatingTemp >= 49 && operatingTemp < 93){
			outCarbonSteelCorrosionSpeedTableYIndex=3;
		}else if(operatingTemp >= 94 && operatingTemp < 121){
			outCarbonSteelCorrosionSpeedTableYIndex=4;
		}else if(operatingTemp >= 121){
			outCarbonSteelCorrosionSpeedTableYIndex=5;
		}
   
//	               横坐标的索引（环境因素）
	int outCarbonSteelCorrosionSpeedTableXIndex=0;
    switch (geographyEnvironment){
        case 1: //1代表'热带/海上'
            outCarbonSteelCorrosionSpeedTableXIndex=0;
            break;
        case 2://2代表'温带/温和'
            outCarbonSteelCorrosionSpeedTableXIndex=1;
            break;
        case 3://3代表'干旱/沙漠'
            outCarbonSteelCorrosionSpeedTableXIndex=2;
            break;
        default:
            outCarbonSteelCorrosionSpeedTableXIndex=1;
            break;
    }

//	                碳钢和低合金钢的外部腐蚀速率表格，按照22610.4 表I.3
    double[][] outCarbonSteelCorrosionSpeedArr={
        {0,0,0},
        {0.13,0.076,0.025},
        {0.05,0.025,0},
        {0.13,0.05,0.025},
        {0.025,0,0},
        {0,0,0}
    };
    double outCarbonSteelCorrosionSpeed = outCarbonSteelCorrosionSpeedArr[outCarbonSteelCorrosionSpeedTableYIndex][outCarbonSteelCorrosionSpeedTableXIndex];
//	                根据涂层质量的调整
    if(coatingStatus==0) {
        switch (coatingStatus) {
            case 1:
                break;
            case 2:
                useDate = useDate + 5;
                break;
            case 3:
                useDate = useDate + 15;
                break;
            default:
                break;
        }
    }
//	                根据管支架补偿的调整

    if(isPipeSupport==1){
        outCarbonSteelCorrosionSpeed=outCarbonSteelCorrosionSpeed*2;
    }
//	                根据界面补偿调整
    if(isInterfaceCompensation==1){
        outCarbonSteelCorrosionSpeed=outCarbonSteelCorrosionSpeed*2;
    }
//	                外部损伤因子的实质是减薄损伤因子
    outDamageFactor = calculate.getReductionDamageFactor(
      part,
      outCarbonSteelCorrosionSpeed,
      useDate,
      thickness,
      checkTime,
      checkValidity,
      linkType,
      isMaintenanceAsRequired,
      tankFoundationSettlement
  );
    break;
//-------------------碳钢和低合金钢的CUI腐蚀-------------------------------------------------------
//2代表"碳钢和低合金钢的CUI腐蚀"
	case 2: 
//纵坐标索引（运行温度）
	int outCarbonSteelCUICorrosionSpeedTableYIndex=0;
	if(operatingTemp<-12){
		outCarbonSteelCUICorrosionSpeedTableYIndex=0;
	}else if(operatingTemp >= -12 && operatingTemp < 15){
		outCarbonSteelCUICorrosionSpeedTableYIndex=1;
	}else if(operatingTemp >= 15 && operatingTemp < 49){
		outCarbonSteelCUICorrosionSpeedTableYIndex=2;
	}else if(operatingTemp >= 49 && operatingTemp < 93){
		outCarbonSteelCUICorrosionSpeedTableYIndex=3;
	}else if(operatingTemp >= 93 && operatingTemp < 121){
		outCarbonSteelCUICorrosionSpeedTableYIndex=4;
	}else if(operatingTemp >= 121){
		outCarbonSteelCUICorrosionSpeedTableYIndex=5;
	}
	//	               横坐标的索引（环境因素）
	int outCarbonSteelCUICorrosionSpeedTableXIndex=0;
	switch (geographyEnvironment){
	    case 1:  //1代表'热带/海上'
	        outCarbonSteelCUICorrosionSpeedTableXIndex=0;
	        break;
	    case 2: //2代表'温带/温和'
	        outCarbonSteelCUICorrosionSpeedTableXIndex=1;
	        break;
	    case 3: //3代表'干旱/沙漠'
	        outCarbonSteelCUICorrosionSpeedTableXIndex=2;
	        break;
	}
	
	//	                碳钢和低合金钢的外部腐蚀速率表格，按照22610.4 表I.3
	    double[][] outCarbonSteelCUICorrosionSpeedArr = {
	        {0,0,0},//小于-12
	        {0.05,0.03,0.01},
	        {0.02,0.01,0},
	        {0.10,0.05,0.02},
	        {0.02,0.01,0},
	        {0,0,0}
	    };
    double outCarbonSteelCUICorrosionSpeed = outCarbonSteelCUICorrosionSpeedArr[outCarbonSteelCUICorrosionSpeedTableYIndex][outCarbonSteelCUICorrosionSpeedTableXIndex];
//	                根据涂层质量的调整
    if(coatingStatus==0) {
        switch (coatingStatus) {
            case 1:
                useDate = useDate + 0;
                break;
            case 2:
                useDate = useDate + 5;
                break;
            case 3:
                useDate = useDate + 15;
                break;
        }
    }
//	                根据管道复杂度调整
    switch(pipeComplexity){
        case 1://低于平均水平
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*0.75;
            break;
        case 2://平均水平
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.0;
            break;
        case 3://高于平均水平
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.25;
            break;
        default:
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.0;
            break;
    }
//	                根据保温层状况调整
    switch(KeepWarmStatus){
        case 1://低于平均水平
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*0.75;
            break;
        case 2://平均水平
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.0;
            break;
        case 3://高于平均水平
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.25;
            break;
        default:
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.0;
            break;
    }
//	                根据管支架补偿的调整
    
    if(isPipeSupport == 1){
        outCarbonSteelCUICorrosionSpeed = outCarbonSteelCUICorrosionSpeed*2;
    }
//	                根据界面补偿调整
    if(isInterfaceCompensation == 1){
        outCarbonSteelCUICorrosionSpeed = outCarbonSteelCUICorrosionSpeed*2;
    }
//	          外部损伤因子的实质是减薄损伤因子
    outDamageFactor = calculate.getReductionDamageFactor(
        part,
        outCarbonSteelCUICorrosionSpeed,
        useDate,
        thickness,
        checkTime,
        checkValidity,
        linkType,
        isMaintenanceAsRequired,
        tankFoundationSettlement
    );
    break;

// -------------------------------------------奥氏体不锈钢的外部SCC--------------------------------------------
case 3://3代表"奥氏体不锈钢的外部SCC"
//	   纵坐标索引（运行温度）
	int outAusteniticCorrosionSpeedTableYIndex=2;
	if(operatingTemp < 60){
		outAusteniticCorrosionSpeedTableYIndex = 0;
	}else if(operatingTemp >= 60 && operatingTemp < 93){
		outAusteniticCorrosionSpeedTableYIndex = 1;
	}else if(operatingTemp >= 93 && operatingTemp < 149){
		outAusteniticCorrosionSpeedTableYIndex = 2;
	}else if(operatingTemp >= 149){
		outAusteniticCorrosionSpeedTableYIndex = 3;
	}
	//	               横坐标的索引（环境因素）
	int outAusteniticCorrosionSpeedTableXIndex=1;
	switch (geographyEnvironment){
	    case 1: //1代表'热带/海上'
	        outAusteniticCorrosionSpeedTableXIndex=0;
	        break;
	    case 2://2代表'温带/温和'
	        outAusteniticCorrosionSpeedTableXIndex=1;
	        break;
	    case 3://3代表'干旱/沙漠'
	        outAusteniticCorrosionSpeedTableXIndex=2;
	        break;
	}

//	                奥氏体不锈钢的外部SCC表格，按照22610.4 表I.17  0代表无 1 代表低 2 代表中  3 代表高
    int[][] outAusteniticSensitiveArr = {
        {0,0,0},//小于60
        {2,1,0},
        {1,1,0},
        {0,0,0}
    };
    int outAusteniticSensitive = outAusteniticSensitiveArr[outAusteniticCorrosionSpeedTableYIndex][outAusteniticCorrosionSpeedTableXIndex];
    int outAusteniticSeverity = 1;
    switch(outAusteniticSensitive){
        case 0:
            outDamageFactor = 0;
        case 1:
            outAusteniticSeverity = 1;
            break;
        case 2:
            outAusteniticSeverity = 10;
            break;
        case 3:
            outAusteniticSeverity = 50;
            break;
    }
//	                根据涂层质量调整涂层涂装日期
    if(coatingStatus == 1){
        switch(coatingStatus){
            case 1:
                coatingUseDate = coatingUseDate + 0;
                break;
            case 2:
                coatingUseDate = coatingUseDate + 5;
                break;
            case 3:
                coatingUseDate = coatingUseDate + 15;
                break;
        }
    }
    if(outAusteniticSensitive != 0){
    	outDamageFactor = calculate.getSCCDamageFactor(
    			operatingTemp,
                designTemp,
                SCCMechanismId,
                checkTime,
                checkValidity,
                outAusteniticSeverity,
                isMediumWater,
                SCCWaterH2S,
                SCCWaterpH,
                isHeatTreatAfterWeld,
                SCCBHardness,
                clConcentration,
                isHeatTreacing,
                NaOHConcentration,
                isSteamBlowing,
                isStressRelief,
                SCCHeatHistory,
                SCCisShutdownProtect,
                surrounding,
                SCCSteelSulfur,
                SCCWaterCarbonateConcentration
    		  );
    }
    break;
case 4:
//4代表"奥氏体不锈钢CUI外部SCC"
//纵坐标索引（运行温度）
	int outAusteniticCUISensitiveTableYIndex=2;
	if(operatingTemp < 60){
		outAusteniticCUISensitiveTableYIndex = 0;
	}else if(operatingTemp >= 60 && operatingTemp < 93){
		outAusteniticCUISensitiveTableYIndex = 1;
	}else if(operatingTemp >= 93 && operatingTemp < 149){
		outAusteniticCUISensitiveTableYIndex = 2;
	}else if(operatingTemp >= 149){
		outAusteniticCUISensitiveTableYIndex = 3;
	}
//	               横坐标的索引（环境因素）
	int outAusteniticCUISensitiveTableXIndex = 1;
    switch (geographyEnvironment){
        case 1: //1代表'热带/海上'
            outAusteniticCUISensitiveTableXIndex = 0;
            break;
        case 2://2代表'温带/温和'
            outAusteniticCUISensitiveTableXIndex = 1;
            break;
        case 3://3代表'干旱/沙漠'
            outAusteniticCUISensitiveTableXIndex = 2;
            break;
    }
//	                奥氏体不锈钢的CUI外部SCC表格，按照22610.4 表I.17  0代表无 1 代表低 2 代表中  3 代表高
    int[][] outAusteniticCUISensitiveArr = {
        {0,0,0},//小于60
        {3,2,1},
        {2,1,0},
        {0,0,0}
    };
    outAusteniticSensitive = outAusteniticCUISensitiveArr[outAusteniticCUISensitiveTableYIndex][outAusteniticCUISensitiveTableXIndex];
//	               根据涂层质量调整涂装时间
    if(coatingStatus == 1){
        switch(coatingStatus){
            case 1:
                coatingUseDate = coatingUseDate+0;
                break;
            case 2:
                coatingUseDate = coatingUseDate+5;
                break;
            case 3:
                coatingUseDate = coatingUseDate+15;
                break;
        }
    }

    if(coatingStatus == 0) {
        switch (coatingStatus) {
            case 1:
                coatingUseDate = coatingUseDate + 0;
                break;
            case 2:
                coatingUseDate = coatingUseDate + 5;
                break;
            case 3:
                coatingUseDate = coatingUseDate + 15;
                break;
        }
    }
//	                根据管道复杂度调整
    switch(pipeComplexity){
        case 1://低于平均水平
            outAusteniticSensitive = outAusteniticSensitive-1;
            break;
        case 2://平均水平
            outAusteniticSensitive = outAusteniticSensitive+0;
            break;
        case 3://高于平均水平
            outAusteniticSensitive = outAusteniticSensitive+1;
            break;
    }
//	                根据保温层状况调整
    if(isKeepWarm == 1){
        switch(KeepWarmStatus){
            case 1://低于平均水平
                outAusteniticSensitive = outAusteniticSensitive-1;
                break;
            case 2://平均水平
                outAusteniticSensitive = outAusteniticSensitive+0;
                break;
            case 3://高于平均水平
                outAusteniticSensitive = outAusteniticSensitive+1;
                break;
            default:
                outAusteniticSensitive = outAusteniticSensitive+0;
                break;
        }
    }
    //                根据保温层含氯情况调整

        if(isKeepWarmHasCL == 0){
            outAusteniticSensitive = outAusteniticSensitive-1;
        }
        int outAusteniticCUISeverity = 1;
        switch(outAusteniticSensitive){
            case 0:
                outDamageFactor = 0;
            case 1:
                outAusteniticCUISeverity = 1;
                break;
            case 2:
                outAusteniticCUISeverity = 10;
                break;
            case 3:
                outAusteniticCUISeverity = 50;
                break;
            default :
                outAusteniticCUISeverity = 1;
                break;
        }
        if(outAusteniticSensitive != 0) {
        	outDamageFactor = calculate.getSCCDamageFactor(
        			operatingTemp,
                    designTemp,
                    SCCMechanismId,
                    checkTime,
                    checkValidity,
                    outAusteniticCUISeverity,
                    isMediumWater,
                    SCCWaterH2S,
                    SCCWaterpH,
                    isHeatTreatAfterWeld,
                    SCCBHardness,
                    clConcentration,
                    isHeatTreacing,
                    NaOHConcentration,
                    isSteamBlowing,
                    isStressRelief,
                    SCCHeatHistory,
                    SCCisShutdownProtect,
                    surrounding,
                    SCCSteelSulfur,
                    SCCWaterCarbonateConcentration
        		  );
        }
        
    } 
	return outDamageFactor;
  }
//-----------------------------------------------脆性断裂损伤因子---------------------------------------------------
//脆性断裂损伤因子没有，默认为0
 
  
//--------------------------------------计算失效后果----------------------------------------------------------------
  ///  壁板的失效后果计算，每一层单独计算  参考C.13.2
  /// </summary> //
  /// <param name="fTankDiameter_">罐直径，单位：m</param>
  /// <param name="fCHT_">储罐单层壁板高度，单位：m</param>
  /// <param name="iFloor_">壁板第几层</param>
  /// <param name="fAcceptBaseQ_">为失效后果可接受水平的基准,单位：万元</param>
  /// <param name="iEnvSensitibility_">环境敏感度.  0-低、1-中、2-高</param>
//  / <param name="eLeakHoleSize_">泄露孔尺寸大小.枚举类型变量</param>
  /// <param name="fHliq_">泄露孔上方的液体高度（可能就是储罐内的实际液体高度），单位：m</param>
  /// <param name="fMatCost_">材料价格系数。材料为Q235A，则取1.0，其他材料的材料价格系数为与Q235A材料实际价格的比值</param>
  /// <param name="fProd_">停产造成的损失。单位：万元</param>
  /// <param name="fPlvdike_">溢出围堪的流体百分比</param>
  /// <param name="fPonsite_">溢出围堪但仍在罐区内，地表土壤中的流体百分比</param>
  /// <param name="fPoffsite_">溢出围堪且已流到罐区外，地表土壤中的流体百分比</param>
  /// <param name="fBblL_leak1_">泄放情况下，小泄露孔对应的流体泄放量，m3</param>
  /// <param name="fBblL_leak2_">泄放情况下，中泄露孔对应的流体泄放量，m3</param>
  /// <param name="fBblL_leak3_">泄放情况下，大泄露孔对应的流体泄放量，m3</param>
  /// <returns>0-A, 1-B, 2-C,3-D,4-E</returns>
  public double getFailureWallConsequence(
		  double  fTankDiameter_,      //罐直径
	      double  fCHT_,               //储罐单层壁板高度
	      int     iFloor_,             //第几层壁板
	      int     iEnvSensitibility_,  //失效后果可接受的基准 单位：万元
	      double  fHliq_,              //泄露孔上方的液体高度 
	      double  fMatCost_,           //材料价格系数
	      double  fProd_,              //停产造成的损失
	      double  fPlvdike_,           //溢出围堰的流体百分比
	      double  fPonsite_,           //溢出围堪但仍在罐区内，地表土壤中的流体百分比
	      double  fPoffsite_           //溢出围堪且已流到罐区外，地表土壤中的流体百分比
		  ){
  if(iEnvSensitibility_ >= 3){
	  iEnvSensitibility_ = 2;
  }
  //壁板不同尺寸泄露孔和破裂的平均失效概率 1-小，2-中，3-大，4-破裂
  double fG1 = 0.00007; double fG2 = 0.000025; double fG3 = 0.000006; double fG4 = 0.0000001;
  //泄露总失效概率 参考步骤8.3
  double fGTotal = fG1 + fG2 + fG3 + fG4;

  //确定泄露孔直径(单位：mm) 参考表C.3

  double fLeakHole1_small_Dia   = 3.0;
  double fLeakHole2_middle_Dia  = 6.0;
  double fLeakHole3_large_Dia   = 50.0;
  double fLeakHole4_Rupture_Dia = 1000*fTankDiameter_/4.0;

  //不同泄露孔对应的泄放速率计算 参考C.4.2
  double fW1_small    =  0.086 * 0.61 * Math.PI * Math.pow(fLeakHole1_small_Dia, 2) / 4 * Math.sqrt(2 * 9.81 * fHliq_);
  double fW2_middle   =  0.086 * 0.61 * Math.PI * Math.pow(fLeakHole2_middle_Dia, 2) / 4 * Math.sqrt(2 * 9.81 * fHliq_);
  double fW3_large    =  0.086 * 0.61 * Math.PI * Math.pow(fLeakHole3_large_Dia, 2) / 4 * Math.sqrt(2 * 9.81 * fHliq_);
  double fW4_rupture  =  0.086 * 0.61 * Math.PI * Math.pow(fLeakHole4_Rupture_Dia, 2) / 4 * Math.sqrt(2 * 9.81 * fHliq_);

  //确定第i层壁板上的液体高度 参考步骤4.1
  double fLHTabove = fHliq_ - (iFloor_ - 1) * fCHT_;
  //确定第i层壁板上的液体体积 参考步骤4.2
  double fLvol_above = Math.PI * Math.pow(fTankDiameter_, 2) / 4 * fLHTabove;

  //确定不同泄露孔尺寸的流体有效泄放量 参考步骤4.3和4.4
  double fBbl_avail1_small   = fLvol_above;
  double fBbl_avail2_middle  = fLvol_above;
  double fBbl_avail3_large   = fLvol_above;
  double fBbl_avail4_rupture = fLvol_above;

  //泄放检测时间 参考步骤6.2
  // double ftld = 7;
  //确定不同泄露孔尺寸的泄放持续时间
  double fld1_small;
  double fld2_middle;
  double fld3_large;
  double fld4_rupture;
  if (fLeakHole1_small_Dia > 3){
       fld1_small = Math.min(fBbl_avail1_small / fW1_small, 1.0);
  } else{
       fld1_small = Math.min(fBbl_avail1_small / fW1_small, 7.0);
  }
  if (fLeakHole2_middle_Dia > 3){
       fld2_middle = Math.min(fBbl_avail2_middle / fW2_middle, 1.0);
  } else{
       fld2_middle = Math.min(fBbl_avail2_middle / fW2_middle, 7.0);
  }
  if (fLeakHole3_large_Dia > 3){
       fld3_large = Math.min(fBbl_avail3_large / fW3_large, 1.0);
  }else{
       fld3_large = Math.min(fBbl_avail3_large / fW3_large, 7.0);
  }
  if (fLeakHole4_Rupture_Dia > 3){
       fld4_rupture = Math.min(fBbl_avail4_rupture / fW4_rupture, 1.0);
  }else{
       fld4_rupture = Math.min(fBbl_avail4_rupture / fW4_rupture, 7.0);
  }

  //确定不同泄露孔尺寸的流体泄放量  参考步骤6.4
  double fBblL_leak1 = Math.min(fW1_small * fld1_small, fBbl_avail1_small);
  double fBblL_leak2 = Math.min(fW2_middle * fld2_middle, fBbl_avail2_middle);
  double fBblL_leak3 = Math.min(fW3_large * fld3_large, fBbl_avail3_large);

  //确定破裂情况下的对应的流体泄放量
  double fBbl_rupture = fld4_rupture;


  double fBbl_Total = fHliq_ * Math.pow(fTankDiameter_, 2) * Math.PI / 4;

  ///环境损失经济后果

  ///不同环境敏感度下的各子项环境经济后果（单位：万元）. 分别为低（0）、中（1）、高（2）。如果不是数组，表示相同
  //介质泄放到围堰区时的环境经济后果
  double fEnvCindike              = 0.04;
  //介质泄放到罐区内土壤表面时的环境经济后果
  double fEnvCss_onsite           = 0.2;
  //介质泄放到罐区外土攘表面时的环境经济后果
  double[] faEnvCss_offsite       = {0.4,1,2};
  //介质泄放到地下土壤内时的环绕经济后果
  //double[] faEnvCsubsoil          = {2, 6, 12};
  //介质泄放到地下水内时的环境经济后果
  //double[] faEnvCgroundwater      = {4, 20,40};
  //介质泄放到地表水内时的环境经济后果
  double[] faEnvCwater            = {2, 6, 20};

  ///泄露相关
  //壁板泄露的流体已泄放总体积 参考步骤8.4
  double fBblL_release = (fBblL_leak1 * fG1 + fBblL_leak2 * fG2 + fBblL_leak3 * fG3 + fBbl_rupture * fG4) / fGTotal;

  //泄漏空泄放后仍在围堰内的流体总体积 参考步骤8.5
  double fBblL_indike = fBblL_release * (1 - fPlvdike_);
  //泄放到罐区内土壤表面的流体总体积 参考步骤8.5
  double fBblL_ss_onsite = fPonsite_ * (fBblL_release - fBblL_indike);
  //泄放到罐区外土壤表面的流体总体积 参考步骤8.5
  double fBblL_ss_offsite = fPoffsite_ * (fBblL_release - fBblL_indike - fBblL_ss_onsite);
  //已到达水源的流体总体积  参考步骤8.5
  double fBblL_water = fBblL_release - (fBblL_indike + fBblL_ss_onsite + fBblL_ss_offsite);
  //泄露导致的环境损失 参考步骤8.6
  double fFCenv_leak = fBblL_indike * fEnvCindike + fBblL_ss_onsite * fEnvCss_onsite + fBblL_ss_offsite * faEnvCss_offsite[iEnvSensitibility_] + fBblL_water * faEnvCwater[iEnvSensitibility_];

  ///破裂相关的体积
  //破裂情况下的流体泄放总体积
  double fBblR_release = fBbl_Total * fG4/fGTotal;
  //破裂情况下仍在围堰内的流体总体积
  double fBblR_indike = fBblR_release*(1-fPlvdike_);
  //破裂情况下在罐区内土壤表面的流体总体积
  double fBblR_ss_onsite = fPonsite_ *(fBblR_release - fBblR_indike);
  //破裂情况下在罐区外土壤表面的流体总体积
  double fBblR_ss_offsite = fPoffsite_ * (fBblR_release - fBblR_indike - fBblR_ss_onsite);
  //破裂情况下已到达水源的流体总体积
  double fBblR_water = fBblR_release - (fBblR_indike + fBblR_ss_onsite + fBblR_ss_offsite);

  //破裂导致的环境损失经济后果
  double fFCenv_rupture = fBblR_indike * fEnvCindike + fBblR_ss_onsite * fEnvCss_onsite + fBblR_ss_offsite * faEnvCss_offsite[iEnvSensitibility_] + fBblR_water * faEnvCwater[iEnvSensitibility_];

  double fFC_Eviron = fFCenv_leak + fFCenv_rupture;

  ///泄露导致的设备损坏经济后果
  double fFC_Emd = fMatCost_ * (fG1 * 4 + fG2 * 9.6 + fG3 * 16 + fG4 * 32) / fGTotal;

  ///泄露的停产损失经济后果
  double fFC_Prod = fProd_ * (fG1 * 2 + fG2 * 3 + fG3 * 3 + fG4 * 7) / fGTotal;

  double fFCTotal = fFC_Prod + fFC_Emd + fFC_Eviron;

  return fFCTotal;
  }
  
// <summary>
  // 底板失效后果计算。参考标准GBT-30578中附录C
  // </summary> ，单位：m</param>
  // <param name="fHliq_">罐内的液体高度，单位：m</param>
  // <param name="fAcceptBaseQ_">为失效后果可接受水平的基准,单位：万元</param>
  // <param name="iEnvSensitibility_">环境敏感度.  0-低、1-中、2-高</param>
  // <param name="eLeakHoleSize_">泄露孔尺寸大小.枚举类型变量</param>
  // <param name="fMatCost_">材料价格系数。材料为Q235A，则取1.0，其他材料的材料价格系数为与Q235A材料实际价格的比值</param>
  // <param name="fProd_">停产造成的损失。单位：万元</param>
  // <param name="fPlvdike_">溢出围堪的流体百分比</param>
  // <param name="fPonsite_">溢出围堪但仍在罐区内，地表土壤中的流体百分比</param>
  // <param name="fPoffsite_">溢出围堪且已流到罐区外，地表土壤中的流体百分比</param>
  // <param name="iLeakHoleSize_">泄放情况下，小泄露孔对应的流体泄放量，m3</param>
  // <param name="fSgw_">罐底到地下水的距离，单位：m</param>
  // <param name="fMedium_p_">介质密度,单位：kg/m3;</param> 。介质密度和动力粘度可以让用户筛选(表C.2)，或者直接填写
  // <param name="fMedium_DynVisc_">介质动力粘度,单位：N*s/m2 或 pa*s;</param> 动力粘度：Dynamic viscosity
  // <param name="iTankBaseType_">储罐基础形式。0---基础为水泥或沥青  1——基础设有RPB，2——基础没有RPB</param>
  // <param name="eTankSubsoilType_">储罐基础下面土壤类型</param>
  // <returns>0-A, 1-B, 2-C,3-D,4-E</returns>
  public double getFailurefloorConsequence(
      double fTankDiameter_,     //罐直径
      double fHliq_ ,            //罐内的液体高度
      int    iEnvSensitibility_, //失效后果可接受水平的基准
      double fMatCost_,          //材料价格系数
      double fProd_,             //停产造成的损失
      double fPlvdike_,          //溢出围堪的流体百分比
      double fPonsite_,          //溢出围堪但仍在罐区内，地表土壤中的流体百分比
      double fPoffsite_,         //溢出围堪且已流到罐区外，地表土壤中的流体百分比
      double fSgw_,              //罐底到地下水的距离
      double fMedium_p_,         //介质密度
      double fMedium_DynVisc_,   //介质动力粘度系数
      int    iTankBaseType_,     //储罐基础形式
      int    eTankSubsoilType_   //储罐基础下面土壤类型 1代表粗砂 2代表细砂 3精细砂 4代表粉砂 5代表含砂黏土 6代表黏土 7代表混凝土-沥青
  ){

	  if(iEnvSensitibility_ >= 2){
		  iEnvSensitibility_=2;
	  }
	  
      //底板不同尺寸泄露孔和破裂的平均失效概率 1-小，2-中，3-大，4-破裂
      double fG1 = 0.00007; double fG2 = 0.000025; double fG3 = 0.000006; double fG4 = 0.0000001;
      //泄露总失效概率
      double fGTotal = fG1 + fG2 + fG3 + fG4;

      /// 土壤孔隙率， 水压传导率的下限值，水压传导率的上限值
//      1代表粗砂 2代表细砂 3精细砂 4代表粉砂 5代表含砂黏土 6代表黏土 7代表混凝土-沥青
      double fPs ;
      double fKh_water_lb ;
      double fKh_water_ub ;
      switch(eTankSubsoilType_)
      {
          case 1:
               fPs = 0.33;
               fKh_water_lb = 0.001;
               fKh_water_ub = 0.0001;
              break;
          case 2:
              fPs = 0.33;
              fKh_water_lb = 0.0001;
              fKh_water_ub = 0.00001;
              break;
          case 3:
              fPs = 0.33;
              fKh_water_lb = 0.00001;
              fKh_water_ub = 0.0000001;
              break;
          case 4:
              fPs = 0.41;
              fKh_water_lb = 0.0000001;
              fKh_water_ub = 0.00000001;
              break;
          case 5:
              fPs = 0.45;
              fKh_water_lb = 0.00000001;
              fKh_water_ub = 0.000000001;
              break;
          case 6:
              fPs = 0.5;
              fKh_water_lb = 0.000000001;
              fKh_water_ub = 0.0000000001;
              break;
          case 7:
              fPs = 0.99;
              fKh_water_lb = 0.000000000001;
              fKh_water_ub = 0.0000000000001;
              break;
          default:
              fPs = 0.33;
              fKh_water_lb = 0.001;
              fKh_water_ub = 0.0001;
              break;
      }
      double fKh_water = 4.32 * 10000 * (fKh_water_lb + fKh_water_ub);
      double fpw =1000;//水的密度:kg/m3
      double fuw = 0.001;//水的动力粘度 N*s/m2 或 pa*s
      double fKh = fKh_water*(fMedium_p_/fpw)*(fuw/fMedium_DynVisc_);//土壤的水压传导率
      double fVels_prod = fKh/fPs;///介质渗透速率

      //小、中、大、破裂泄露孔尺寸对应的泄露孔直径.单位：mm
      //确定泄露孔尺寸，参考表C.4
      double fLeakHoleDia_small   = 3.0;
      double fLeakHoleDia_middle  = 0;
      double fLeakHoleDia_large   = 0; 
      //double fLeakHoleDia_Rupture = 0;
      if (iTankBaseType_ == 1)
      {
           fLeakHoleDia_small   = 3.0;
      }else{
           fLeakHoleDia_small   = 13.0;
           fLeakHoleDia_middle  = 0;
           fLeakHoleDia_large   = 0;
           //fLeakHoleDia_Rupture = 1000 * fTankDiameter_ / 4.0;
      }
      //不同泄露孔尺寸对应泄露孔数量 参考表C.5
      double iLeakHole_middle_num = 0;
      double iLeakHole_large_num  = 0;
      double iLeakHole_small_num  = Math.max((Math.round((Math.pow((fTankDiameter_/30),2)))),1);

      //泄放检测时间
      //C.8.2 参考步骤7.2 0代表水泥和沥青 1代表设有RPB 2代表没有设RPB
      double ftld = 30.0;
      if(iTankBaseType_ == 2){
          ftld = 30.0;
      }else if(iTankBaseType_ == 1){
          ftld = 360.0;
      }else if(iTankBaseType_ == 0){
          ftld = 7.0;
      }

      ///环境损失经济后果
      ///不同环境敏感度下的各子项环境经济后果（单位：万元）. 分别为低（0）、中（1）、高（2）。如果不是数组，表示相同
      //介质泄放到围堰区时的环境经济后果
      double fEnvCindike = 0.04;
      //介质泄放到罐区内土壤表面时的环境经济后果
      double fEnvCss_onsite = 0.2;
      //介质泄放到罐区外土攘表面时的环境经济后果
      double[] faEnvCss_offsite = {0.4, 1, 2};
      //介质泄放到地下土壤内时的环绕经济后果
      double[] faEnvCsubsoil = {2, 6, 12};
      //介质泄放到地下水内时的环境经济后果
      double[] faEnvCgroundwater = {4, 20, 40};
      //介质泄放到地表水内时的环境经济后果
      double[] faEnvCwater = {2, 6, 20};

      ///泄露相关
      //流体泄放到地下水的时间
      double fTgl = fSgw_ / fVels_prod;

//      //在泄放情况下，分别对应于与小、中、大泄露孔尺寸的地下水中流体的体积
//      $fBblL_gw1 = .0; $fBblL_gw2 = .0; $fBblL_gw3 = .0;
//
//      //不同泄露孔尺寸下，储罐底板的泄放速率
//      $fW1_small = .0; $fW2_middle = .0; $fW3_large = .0;


      //表征土壤的接触程度调整系数
      //参考C4.3.2
      double fCqo ;
      double fhliq ;
      if(iTankBaseType_ == 1)
      {
          //如果设有防泄漏隔离屏后果分析的取值
          fCqo = 0.21;
          fhliq = 0.08;
      }else{
          fCqo = 1.15;//表征土壤接触程度的调整系数
          fhliq = fHliq_;//储罐内的液体高度
      }

      //储罐底板的泄放速率计算  参考步骤3.4
      double fW1_small;
      if(fKh > (86.4 * Math.pow(fLeakHoleDia_small, 2.0))){
          fW1_small = 0.01296 * Math.PI * fLeakHoleDia_small * iLeakHole_small_num * Math.sqrt(2*9.81*fhliq);
      } else{
          fW1_small = 0.3787 * fCqo * Math.pow(fLeakHoleDia_small, 0.2) * iLeakHole_small_num * Math.pow(fhliq, 0.9) * Math.pow(fKh, 0.74);
      }
      
      double fW2_middle;
      if (fKh > (86.4 * Math.pow(fLeakHoleDia_middle, 2.0))){
          fW2_middle = 0.01296 * Math.PI * fLeakHoleDia_middle * iLeakHole_middle_num * Math.sqrt(2*9.81*fhliq);
      } else{
          fW2_middle = 0.3787 * fCqo * Math.pow(fLeakHoleDia_middle, 0.2) * iLeakHole_middle_num * Math.pow(fhliq, 0.9) * Math.pow(fKh, 0.74);
      }
      
      double fW3_large;
      if (fKh > (86.4 * Math.pow(fLeakHoleDia_large, 2.0))){
          fW3_large = 0.01296 * Math.PI * fLeakHoleDia_large * iLeakHole_large_num * Math.sqrt(2 * 9.81 * fhliq);
      } else{
          fW3_large = 0.3787 * fCqo * Math.pow(fLeakHoleDia_large, 0.2) * iLeakHole_large_num * Math.pow(fhliq, 0.9) * Math.pow(fKh, 0.74);
      }

      //储罐内流体的总泄放量 C5.3 步骤5.1和5.2
      double fBbl_Total = fhliq * (Math.PI * Math.pow(fTankDiameter_, 2.0)) / 4.0;

      //确定泄放持续时间 参考步骤7.3
      double fld1_small  = Math.min((fBbl_Total/fW1_small),ftld);
      double fld2_middle = Math.min((fBbl_Total/fW2_middle),ftld);
      double fld3_large  = Math.min((fBbl_Total/fW3_large),ftld);

      //确定不同泄露孔尺寸下的对应的流体泄放量 参考步骤7.4
      double fBblL_leak1 = Math.min((fW1_small * fld1_small),fBbl_Total);
      double fBblL_leak2 = Math.min((fW2_middle * fld2_middle),fBbl_Total);
      double fBblL_leak3 = Math.min((fW3_large * fld3_large),fBbl_Total);
      //double fBblL_leak4 = fBbl_Total;

      //每个泄露孔尺寸泄放到罐底的流体体积 参考附录C中的步骤9.5
      
      double fBblL_gw1;
      double fBblL_gw2;
      double fBblL_gw3;
      if (fTgl > ftld)
      {
          fBblL_gw1 = fBblL_leak1 * (ftld - fTgl) / ftld;
          fBblL_gw2 = fBblL_leak2 * (ftld - fTgl) / ftld;
          fBblL_gw3 = fBblL_leak3 * (ftld - fTgl) / ftld;
      }
      else
      {
          fBblL_gw1 = 0;
          fBblL_gw2 = 0;
          fBblL_gw3 = 0;
      }

      double fBblL_subsoil1 = fBblL_leak1 - fBblL_gw1;
      double fBblL_subsoil2 = fBblL_leak2 - fBblL_gw2;
      double fBblL_subsoil3 = fBblL_leak3 - fBblL_gw3;

      //泄露导致的环境损失。步骤9.6
      double fFCenv_leak = ((fBblL_gw1 * faEnvCgroundwater[iEnvSensitibility_] + fBblL_subsoil1 * faEnvCsubsoil[iEnvSensitibility_]) + (fBblL_gw2 * faEnvCgroundwater[iEnvSensitibility_] + fBblL_subsoil2 * faEnvCsubsoil[iEnvSensitibility_]) + (fBblL_gw3 * faEnvCgroundwater[iEnvSensitibility_] + fBblL_subsoil3 * faEnvCsubsoil[iEnvSensitibility_])) / fGTotal;

      ///破裂相关的体积
      //破裂情况下的流体泄放总体积
      double fBblR_release = fBbl_Total * fG4 / fGTotal;
      //破裂情况下仍在围堰内的流体总体积
      double fBblR_indike  = fBblR_release * (1 - fPlvdike_);
      //破裂情况下在罐区内土壤表面的流体总体积
      double fBblR_ss_onsite = fPonsite_ * (fBblR_release - fBblR_indike);
      //破裂情况下在罐区外土壤表面的流体总体积
      double fBblR_ss_offsite = fPoffsite_ * (fBblR_release - fBblR_indike - fBblR_ss_onsite);
      //破裂情况下已到达水源的流体总体积
      double fBblR_water = fBblR_release - (fBblR_indike + fBblR_ss_onsite + fBblR_ss_offsite);
      //破裂导致的环境损失
      double fFCenv_rupture = fBblR_indike * fEnvCindike + fBblR_ss_onsite * fEnvCss_onsite + fBblR_ss_offsite * faEnvCss_offsite[iEnvSensitibility_] + fBblR_water * faEnvCwater[iEnvSensitibility_];

      //参考步骤9.10
      double fFC_Eviron = fFCenv_leak + fFCenv_rupture;
      if(fFC_Eviron < 0){
    	  fFC_Eviron = 0; 
      }
      ///泄露导致的设备损坏经济后果     步骤9.11
      double fFC_Emd = fMatCost_ * (fG1 * 4 +  fG4 * 96 * Math.pow((fTankDiameter_/30.0),2)) / fGTotal;

      ///泄露的停产损失后果  步骤9.12
      double fFC_Prod = fProd_ * (fG1 * 5 +  fG4 * 50) / fGTotal;
 
      //步骤9.13
      double fFCTotal = fFC_Prod + fFC_Emd + fFC_Eviron;
      return fFCTotal;
  }
 
  public double getUseDate(String beginDate){
	  DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	  String nowDate = df.format(new Date());
	  Date one;  
      Date two;  
      long days=0;  
      try {  
          one = df.parse(beginDate);  
          two = df.parse(nowDate);  
          long time1 = one.getTime();  
          long time2 = two.getTime();  
          long diff ;  
          if(time1<time2) {  
              diff = time2 - time1;  
          } else {  
              diff = time1 - time2;  
          }  
          days = diff / (1000 * 60 * 60 * 24 ) / 365;  
      } catch (ParseException e) {  
          e.printStackTrace();
      } 
      return days;
	  
  }
  
  public String addDate(int year){
	  
	  DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	  String reStr = sdf.format(new Date());
	  
	  try {  
		  Date dt=sdf.parse(reStr);
	      Calendar rightNow = Calendar.getInstance();
	      rightNow.setTime(dt);
	      rightNow.add(Calendar.YEAR,year);//日期减1年
	     
	      Date dt1=rightNow.getTime();
	       reStr = sdf.format(dt1); 
      } catch (ParseException e) {  
          e.printStackTrace();
      }   
      return reStr;
  }
  
}
