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
	 
	 String[] re = new String[5];    //��������
	 
	 Calculate calculate = new Calculate();
	 List<Map<String,Object>> plantInfo                  = calculate.getPlantInfo(PlantId);
     List<Map<String,Object>> wallboardInfo              = calculate.getWallboardInfo(PlantId);
     List<Map<String,Object>> plantMech                  = calculate.getPlantMech(PlantId);
     List<Map<String,Object>> plantTest                  = calculate.getPlantTest(PlantId);
     List<Map<String,Object>> wallPlantCorrosion         = calculate.getWallPlantCorrosion(PlantId);
     List<Map<String,Object>> bottomEdgePlantCorrosion   = calculate.getBottomPlantCorrosion(PlantId,2,1);
     List<Map<String,Object>> bottomMiddlePlantCorrosion = calculate.getBottomPlantCorrosion(PlantId,2,2);
     
     if(wallboardInfo.isEmpty()){
         re[0] = "���豸û����ӱڰ壬��ǰ��������Ϣ������ӱڰ�";
         re[1] = ""+0;
         return re;
     }
     if(plantMech.isEmpty()){
    	 re[0] = "���豸û�н������˻���ɸѡ�����Ƚ������˻���ɸѡ";
    	 re[1] = ""+0;
    	 return re;
     }
     
     double useDate = 0;
     useDate  =  calculate.getUseDate(plantInfo.get(0).get("useDate").toString());
	 
//���վ���ͼ 1 ����ʧЧ�����Եȼ�  2����ʧЧ����ȼ�
	 String[][] riskMatrixTable = new String[6][6];
	 riskMatrixTable[1][1]="�ͷ���";
     riskMatrixTable[1][2]="�ͷ���";
     riskMatrixTable[1][3]="�з���";
     riskMatrixTable[1][4]="�з���";
     riskMatrixTable[1][5]="�и߷���";
     riskMatrixTable[2][1]="�ͷ���";
     riskMatrixTable[2][2]="�ͷ���";
     riskMatrixTable[2][3]="�з���";
     riskMatrixTable[2][4]="�з���";
     riskMatrixTable[2][5]="�и߷���";
     riskMatrixTable[3][1]="�ͷ���";
     riskMatrixTable[3][2]="�ͷ���";
     riskMatrixTable[3][3]="�з���";
     riskMatrixTable[3][4]="�и߷���";
     riskMatrixTable[3][5]="�߷���";
     riskMatrixTable[4][1]="�з���";
     riskMatrixTable[4][2]="�з���";
     riskMatrixTable[4][3]="�и߷���";
     riskMatrixTable[4][4]="�и߷���";
     riskMatrixTable[4][5]="�߷���";
     riskMatrixTable[5][1]="�и߷���";
     riskMatrixTable[5][2]="�и߷���";
     riskMatrixTable[5][3]="�и߷���";
     riskMatrixTable[5][4]="�߷���";
     riskMatrixTable[5][5]="�߷���"; 
//-----------------------------------------���㵱ǰ����---------------------------------------

// ------------------------------------------�ڰ����----------------------------------------
    
     
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
    	 
//-------------------------------------����ڰ���������----------------------------------------
//       1���ڰ�����������
    	 double wallReductionDamageFactor = calculate.getReductionDamageFactor(
                 
                     1,                                                             //����ڰ�
                     Double.parseDouble(WallPlantCorrosion.get("long_termCorrosion").toString()),            //�ò�ڰ帯ʴ����
                     useDate,                                                                                    //ʹ��ʱ��(����ʹ��ʱ��)
                     Double.parseDouble(WallPlantCorrosion.get("namelyThickness").toString()),               //�ڰ��ʼ���(Ŀǰ��������)

                     Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                      //�������
                     plantTest.get(0).get("wallCheckValidity").toString(),                                    //������Ч��
                     
                     plantInfo.get(0).get("wallboardLinkType").toString(),                                    //�ڰ�������ʽ
                     Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //�Ƿ񰴹涨ά��
                     Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //������������
                
             );
    	 wallFactorMap.put("wallReductionDamageFactor", wallReductionDamageFactor);
    	 
//       2���ڰ��ⲿ��������
          double wallOutDamageFactor  = calculate.getOutDamageFactor(
        		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //�����¶�
        		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //����¶�
                  1,                                                                                            //����ڰ�
                  Double.parseDouble(WallPlantCorrosion.get("namelyThickness").toString()),                    //�ڰ��ʼ���(Ŀǰ��������)

                  Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //�ܵ����Ӷ�
                  Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //�Ƿ��²㺬��
                  Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //�Ƿ��֧�ܲ���
                  Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //�Ƿ���油��
                  Integer.parseInt(plantMech.get(0).get("wallOutDamageMechanismId").toString()),                //�ⲿ���˻���
                 
                  useDate,                                                                                        //ʹ��ʱ��(����ʹ��ʱ��)
                  Integer.parseInt(plantInfo.get(0).get("isWallboardKeepWarm").toString()),                                //�Ƿ��б��²�
                  Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),                            //���²�����
                  Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                             //Ϳ������
                  useDate, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),                         //Ϳ��ʹ��ʱ��
                  plantInfo.get(0).get("wallboardLinkType").toString(),                                       //������ʽ
                  
                 Integer.parseInt(plantMech.get(0).get("wallSCCMechanismId").toString()),                   //SCC���˻���
                 Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                            //�������
                 plantTest.get(0).get("wallCheckValidity").toString(),                                           //������Ч��

                 Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //�豸����
                 Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //�Ƿ񰴹涨ά��
                 Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //������������
                 Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //�Ƿ���ʺ�ˮ
                 
                 Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //ˮ�е�H2Sֵ
                 Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //ˮ��PHֵ
                 Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //�Ƿ񺸺��ȴ���
                 Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //�����Ӳ��
                 Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //ˮ��cl���Ӻ���

                 Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //�Ƿ����
                 Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOHŨ��
                 Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //�Ƿ�������ɨ
                 Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //�Ƿ����Ӧ������
                 Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //����ʷ
                 Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //�Ƿ���ͣ������
                 plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //����������
                 Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //�ְ�������
                 Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //̼����Ũ��
         );
          wallFactorMap.put("wallOutDamageFactor", wallOutDamageFactor);
    	 
//     3���ڰ�SCC��������

          double wallSCCDamageFactor = calculate.getSCCDamageFactor(
        		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //�����¶�
        		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //����¶�
        		  Integer.parseInt(plantMech.get(0).get("wallSCCMechanismId").toString()),            //SCC���˻���
        		  Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                 //�������
                  plantTest.get(0).get("wallCheckValidity").toString(),                               //������Ч��
                  0,                                                                                  //�����ⲿ�������ӵ�ʱ����Ҫ���������Ըߵͣ�ֱ���������س̶�ָ��
                 
                  Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //�Ƿ���ʺ�ˮ
                  Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //ˮ�е����⺬��
                  Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //ˮ��pHֵ
                  Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //�Ƿ񺸺��ȴ���
                  Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //�����Ӳ��
                  Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //ˮ�е������Ӻ���

                  Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //�Ƿ����
        		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOHŨ��
        		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //�Ƿ�������ɨ
        		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //�Ƿ����Ӧ������
        		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //����ʷ
        		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //�Ƿ�ͣ������
        		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //����������
        		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //�ְ�������
        		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //̼����Ũ��
              );
              wallFactorMap.put("wallSCCDamageFactor", wallSCCDamageFactor);
             
//            4���ڰ���Զ�������
              wallFactorMap.put("wallBrittleDamageFactor", 0);
           
            //------------------------------------������������-----------------------------------------------------------

//            ����õ��������ӵ�ֵ
       double  wallDamageFactor = calculate.getDamageFaction(
                0,                                     //��ʴ���ͣ��ڰ�Ϊ���ȸ�ʴ���װ�Ϊ�ֲ���ʴ 0������ȸ�ʴ 1����ֲ���ʴ
                wallReductionDamageFactor,     //������������
                wallOutDamageFactor,           //�ⲿ��������
                wallSCCDamageFactor,           //Ӧ����ʴ������������
                0             //���Զ�����������
            );
       wallFactorMap.put("wallDamageFactor", wallDamageFactor);
               
//             �ڰ�ʧЧ���
       double wallFailConsequence = calculate.getFailureWallConsequence(
    		   Double.parseDouble(plantInfo.get(0).get("D").toString()),                            //��ֱ��
    		   Double.parseDouble(WallPlantCorrosion.get("height").toString()),         //���޵���ڰ�߶�
    		   Integer.parseInt(WallPlantCorrosion.get("layerNO").toString()),           //�ڼ���ڰ�
               Integer.parseInt(plantInfo.get(0).get("sensitiveEnvironment").toString()),             //ʧЧ����ɽ��ܵĻ�׼ ��λ����Ԫ
    		   Double.parseDouble(plantInfo.get(0).get("fillH").toString()),                         //й¶���Ϸ���Һ��߶�
    		   Double.parseDouble(plantInfo.get(0).get("fMatCost_").toString()),                     //���ϼ۸�ϵ��
    		   Double.parseDouble(plantInfo.get(0).get("stopLoss").toString()),                      //ͣ����ɵ���ʧ
    		   Double.parseDouble(plantInfo.get(0).get("overflowPercentage").toString()),            //���Χ�ߵ�����ٷֱ�
    		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageIn").toString()),          //���Χ�������ڹ����ڣ��ر������е�����ٷֱ�
    		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageOut").toString())          //���Χ���������������⣬�ر������е�����ٷֱ�
           ); 
       wallFactorMap.put("wallFailConsequence", wallFailConsequence);
       wallFactor.add(wallFactorMap);
    	 
    	 //��ȡ������������ֵ����ֵ
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
    
//-----------------------------�ڰ���������---------------------------------------------------------
//   �ҳ��ڰ������������ӣ���������������Ӽ�����յȼ�     
  double fAcceptBaseQ_             =    Double.parseDouble(plantInfo.get(0).get("failConseqenceAccept").toString());        //ʧЧ����ɽ��ܻ�׼
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
  
//�ڰ�ʧЧ������
 double wallAverageFailurePro = calculate.getAverageFailurePro(0,  Float.parseFloat(plantInfo.get(0).get("breakSize").toString()));          //�ڰ�ƽ��ʧЧ������
 double wallFailurePro        = calculate.getFailurePro(1, wallAverageFailurePro, maxWallDamageFactor);  //�ڰ�ʧЧ������
 int    wallFailureProLevel   = 1;
//����ڰ�ʧЧ�����Եȼ�
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

 
//-----------------------------------------����װ���յȼ�--------------------------------------

 double wallRisk      = maxWallFailConsequence * wallFailurePro;                              //�ڰ����
 String wallRiskLevel = riskMatrixTable[wallFailureProLevel][wallFailConsequenceLevelIndex];  //�ڰ���յȼ�

 
//-------------------------�װ���ռ���ָ���-----------------------------------------------------
 
 
 double  maxBottomEdgeDamageFactor    = 0;
 double  maxBottomEdgeFailConsequence = 0;
 double  maxBottomEdgeCorrosion       = 0;
 String  maxBottomEdgeValueLayerId        = "";
 
 List<Map<String, Object>> bottomEdgeFactor = new ArrayList<Map<String, Object>>(); 
 
 for (Map<String, Object>  bottomEdgeCorrosion : bottomEdgePlantCorrosion)  
 {  
	 Map<String, Object>       bottomEdgeFactorMap = new HashMap<String, Object>(); 
//-------------------------------------��Ե�����----------------------------------------
//   1����Ե�����������
	 double bottomEdgeReductionDamageFactor = calculate.getReductionDamageFactor(
             
                 1,                                                                                       //����ڰ�
                 Double.parseDouble(bottomEdgeCorrosion.get("long_termCorrosion").toString()),            //�ò�ڰ帯ʴ����
                 useDate,                                                                                     //ʹ��ʱ��(����ʹ��ʱ��)
                 Double.parseDouble(plantInfo.get(0).get("bottomEdgeNamelyThickness").toString()),               //�ڰ��ʼ���(Ŀǰ��������)

                 Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                      //�������
                 plantTest.get(0).get("bottomCheckValidity").toString(),                                    //������Ч��
                 
                 plantInfo.get(0).get("bottomLinkType").toString(),                                    //�ڰ�������ʽ
                 Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //�Ƿ񰴹涨ά��
                 Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //������������
            
         );
	 bottomEdgeFactorMap.put("bottomEdgeReductionDamageFactor", bottomEdgeReductionDamageFactor);
	 
//   2����Ե���ⲿ��������
      double bottomEdgeOutDamageFactor  = calculate.getOutDamageFactor(
    		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //�����¶�
    		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //����¶�
              1,                       //����ڰ�
              Double.parseDouble(plantInfo.get(0).get("bottomEdgeNamelyThickness").toString()),                    //�ڰ��ʼ���(Ŀǰ��������)

              Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //�ܵ����Ӷ�
              Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //�Ƿ��²㺬��
              Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //�Ƿ��֧�ܲ���
              Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //�Ƿ���油��
              Integer.parseInt(plantMech.get(0).get("floorOutDamageMechanismId").toString()),                //�ⲿ���˻���
             
              useDate,                                                                                        //ʹ��ʱ��(����ʹ��ʱ��)
              0,                                                                                          //�Ƿ��б��²㣨�װ�û�б��²㣩
              Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),               //���²�����
              Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                //Ϳ������
              useDate, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),               //Ϳ��ʹ��ʱ��
              plantInfo.get(0).get("bottomLinkType").toString(),                                       //������ʽ
              
             Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),                   //SCC���˻���
             Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                            //�������
             plantTest.get(0).get("bottomCheckValidity").toString(),                                           //������Ч��

             Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //�豸����
             Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //�Ƿ񰴹涨ά��
             Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //������������
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //�Ƿ���ʺ�ˮ
             
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //ˮ�е�H2Sֵ
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //ˮ��PHֵ
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //�Ƿ񺸺��ȴ���
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //�����Ӳ��
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //ˮ��cl���Ӻ���

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //�Ƿ����
             Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOHŨ��
             Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //�Ƿ�������ɨ
             Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //�Ƿ����Ӧ������
             Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //����ʷ
             Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //�Ƿ���ͣ������
             plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //����������
             Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //�ְ�������
             Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //̼����Ũ��
     );
      bottomEdgeFactorMap.put("bottomEdgeOutDamageFactor", bottomEdgeOutDamageFactor);
	 
// 3����Ե��SCC��������

      double bottomEdgeSCCDamageFactor = calculate.getSCCDamageFactor(
    		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //�����¶�
    		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //����¶�
    		  Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),            //SCC���˻���
    		  Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                 //�������
              plantTest.get(0).get("bottomCheckValidity").toString(),                               //������Ч��
              0,                                                                                  //�����ⲿ�������ӵ�ʱ����Ҫ���������Ըߵͣ�ֱ���������س̶�ָ��
             
              Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //�Ƿ���ʺ�ˮ
              Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //ˮ�е����⺬��
              Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //ˮ��pHֵ
              Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //�Ƿ񺸺��ȴ���
              Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //�����Ӳ��
              Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //ˮ�е������Ӻ���

              Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //�Ƿ����
    		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOHŨ��
    		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //�Ƿ�������ɨ
    		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //�Ƿ����Ӧ������
    		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //����ʷ
    		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //�Ƿ�ͣ������
    		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //����������
    		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //�ְ�������
    		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //̼����Ũ��
          );
          bottomEdgeFactorMap.put("bottomEdgeSCCDamageFactor", bottomEdgeSCCDamageFactor);
         
//        4����Ե����Զ�������
          bottomEdgeFactorMap.put("bottomEdgeBrittleDamageFactor", 0);
       
        //------------------------------------������������-----------------------------------------------------------

//        ����õ��������ӵ�ֵ
   double  bottomEdgeDamageFactor = calculate.getDamageFaction(
            1,                                     //��ʴ���ͣ��ڰ�Ϊ���ȸ�ʴ���װ�Ϊ�ֲ���ʴ 0������ȸ�ʴ 1����ֲ���ʴ
            bottomEdgeReductionDamageFactor,     //������������
            bottomEdgeOutDamageFactor,           //�ⲿ��������
            bottomEdgeSCCDamageFactor,           //Ӧ����ʴ������������
            0             //���Զ�����������
        );
   bottomEdgeFactorMap.put("bottomEdgeDamageFactor", bottomEdgeDamageFactor);
   bottomEdgeFactor.add(bottomEdgeFactorMap);        

	 
	 //��ȡ������������ֵ����ֵ
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
//-------------------------------------�м�����----------------------------------------
//   1���м�����������
	 double bottomMiddleReductionDamageFactor = calculate.getReductionDamageFactor(
             
                 1,                                                                                       //����ڰ�
                 Double.parseDouble(bottomMiddleCorrosion.get("long_termCorrosion").toString()),            //�ò�ڰ帯ʴ����
                 useDate,                                                                                     //ʹ��ʱ��(����ʹ��ʱ��)
                 Double.parseDouble(plantInfo.get(0).get("bottomMiddleNamelyThickness").toString()),               //�ڰ��ʼ���(Ŀǰ��������)

                 Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                      //�������
                 plantTest.get(0).get("bottomCheckValidity").toString(),                                    //������Ч��
                 
                 plantInfo.get(0).get("bottomLinkType").toString(),                                    //�ڰ�������ʽ
                 Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //�Ƿ񰴹涨ά��
                 Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //������������
            
         );
	 bottomMiddleFactorMap.put("bottomMiddleReductionDamageFactor", bottomMiddleReductionDamageFactor);
	 
//   2����Ե���ⲿ��������
      double bottomMiddleOutDamageFactor  = calculate.getOutDamageFactor(
    		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //�����¶�
    		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //����¶�
              1,                       //����ڰ�
              Double.parseDouble(plantInfo.get(0).get("bottomMiddleNamelyThickness").toString()),                    //�ڰ��ʼ���(Ŀǰ��������)

              Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //�ܵ����Ӷ�
              Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //�Ƿ��²㺬��
              Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //�Ƿ��֧�ܲ���
              Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //�Ƿ���油��
              Integer.parseInt(plantMech.get(0).get("floorOutDamageMechanismId").toString()),                //�ⲿ���˻���
             
              useDate,                                                                                        //ʹ��ʱ��(����ʹ��ʱ��)
              0,                                                                                          //�Ƿ��б��²㣨�װ�û�б��²㣩
              Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),               //���²�����
              Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                //Ϳ������
              useDate, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),               //Ϳ��ʹ��ʱ��
              plantInfo.get(0).get("bottomLinkType").toString(),                                       //������ʽ
              
             Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),                   //SCC���˻���
             Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                            //�������
             plantTest.get(0).get("bottomCheckValidity").toString(),                                           //������Ч��

             Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //�豸����
             Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //�Ƿ񰴹涨ά��
             Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //������������
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //�Ƿ���ʺ�ˮ
             
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //ˮ�е�H2Sֵ
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //ˮ��PHֵ
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //�Ƿ񺸺��ȴ���
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //�����Ӳ��
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //ˮ��cl���Ӻ���

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //�Ƿ����
             Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOHŨ��
             Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //�Ƿ�������ɨ
             Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //�Ƿ����Ӧ������
             Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //����ʷ
             Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //�Ƿ���ͣ������
             plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //����������
             Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //�ְ�������
             Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //̼����Ũ��
     );
      bottomMiddleFactorMap.put("bottomMiddleOutDamageFactor", bottomMiddleOutDamageFactor);
	 
// 3���ڰ�SCC��������

      double bottomMiddleSCCDamageFactor = calculate.getSCCDamageFactor(
    		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //�����¶�
    		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //����¶�
    		  Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),            //SCC���˻���
    		  Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                 //�������
              plantTest.get(0).get("bottomCheckValidity").toString(),                               //������Ч��
              0,                                                                                  //�����ⲿ�������ӵ�ʱ����Ҫ���������Ըߵͣ�ֱ���������س̶�ָ��
             
              Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //�Ƿ���ʺ�ˮ
              Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //ˮ�е����⺬��
              Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //ˮ��pHֵ
              Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //�Ƿ񺸺��ȴ���
              Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //�����Ӳ��
              Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //ˮ�е������Ӻ���

              Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //�Ƿ����
    		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOHŨ��
    		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //�Ƿ�������ɨ
    		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //�Ƿ����Ӧ������
    		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //����ʷ
    		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //�Ƿ�ͣ������
    		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //����������
    		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //�ְ�������
    		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //̼����Ũ��
          );
          bottomMiddleFactorMap.put("bottomMiddleSCCDamageFactor", bottomMiddleSCCDamageFactor);
         
//        4���ڰ���Զ�������
          bottomMiddleFactorMap.put("bottomMiddleBrittleDamageFactor", 0);
       
        //------------------------------------������������-----------------------------------------------------------

//        ����õ��������ӵ�ֵ
   double  bottomMiddleDamageFactor = calculate.getDamageFaction(
            1,                                     //��ʴ���ͣ��ڰ�Ϊ���ȸ�ʴ���װ�Ϊ�ֲ���ʴ 0������ȸ�ʴ 1����ֲ���ʴ
            bottomMiddleReductionDamageFactor,     //������������
            bottomMiddleOutDamageFactor,           //�ⲿ��������
            bottomMiddleSCCDamageFactor,           //Ӧ����ʴ������������
            0             //���Զ�����������
        );
   bottomMiddleFactorMap.put("bottomMiddleDamageFactor", bottomMiddleDamageFactor);
           
   bottomMiddleFactor.add(bottomMiddleFactorMap);
	 
	 //��ȡ������������ֵ����ֵ
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
 
//----------------------------�װ���������------------------------------------------------------
 
		 String floorMajorRisk    =  "";
		 double floorDamageFactor =  0;
		 
		 if(maxBottomEdgeDamageFactor >= maxBottomMiddleDamageFactor){
		      floorMajorRisk    =  maxBottomEdgeValueLayerId;
		      floorDamageFactor =  maxBottomEdgeDamageFactor;
		 }else{
			  floorMajorRisk    =  maxBottomMiddleValueLayerId;
		      floorDamageFactor =  maxBottomMiddleDamageFactor;
		 }
     
//---------------------------------�װ�ʧЧ���--------------------------------------
 double floorFailConsequence = calculate.getFailurefloorConsequence(
		   Double.parseDouble(plantInfo.get(0).get("D").toString()),                          //����ֱ��
		   Double.parseDouble(plantInfo.get(0).get("fillH").toString()),                      //����Һ��߶�
		   Integer.parseInt(plantInfo.get(0).get("sensitiveEnvironment").toString()),       //�������ж�
		   Double.parseDouble(plantInfo.get(0).get("fMatCost_").toString()),                  //���ϼ۸�ϵ��
		   Double.parseDouble(plantInfo.get(0).get("stopLoss").toString()),                   //ͣ����ɵ���ʧ
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentage").toString()),         //���Χ��������ٷֱ�
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageIn").toString()),       //���Χ�������ڹ����ڣ��ر������е�����ٷֱ�
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageOut").toString()),      //���Χ���������������⣬�ر������е�����ٷֱ�
	       Double.parseDouble(plantInfo.get(0).get("bottomToWaterDistance").toString()),      //�޵׵�����ˮ�ľ���
	   	   Double.parseDouble(plantInfo.get(0).get("mediumPercentage").toString()),           //�����ܶ�
		   Double.parseDouble(plantInfo.get(0).get("mediumDyViscosity").toString()),          //���ʶ���ճ��
		   Integer.parseInt(plantInfo.get(0).get("bottomGasket").toString()),               //���޻�����ʽ��0---����Ϊˮ�������  1������������RPB��2��������û��RPB
		   Integer.parseInt(plantInfo.get(0).get("bottomGasketSoil").toString())           //���޻���������������
       ); 
 
 
 
//�ҳ��ڰ������������ӣ���������������Ӽ�����յȼ�     
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

//�ڰ�ʧЧ������
double floorAverageFailurePro = calculate.getAverageFailurePro(1,  Float.parseFloat(plantInfo.get(0).get("breakSize").toString()));          //�ڰ�ƽ��ʧЧ������
double floorFailurePro        = calculate.getFailurePro(1, floorAverageFailurePro, floorDamageFactor);  //�ڰ�ʧЧ������
int    floorFailureProLevel   = 1;
//����ڰ�ʧЧ�����Եȼ�
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

//------------------------------------------------�װ���յȼ�---------------------------------------------------------

double floorRisk      = floorFailConsequence*floorFailurePro;
String floorRiskLevel = riskMatrixTable[floorFailureProLevel][floorFailConsequenceLevelIndex];


//---------------------------------------------����δ�����պ���������---------------------------------------------------
//---------------------------------------------����δ�����պ���������---------------------------------------------------
//---------------------------------------------����δ�����պ���������---------------------------------------------------
//---------------------------------------------����δ�����պ���������---------------------------------------------------
//---------------------------------------------����δ�����պ���������---------------------------------------------------

List<Map<String, Object>> plantFactor_Fu = new ArrayList<Map<String, Object>>(); 


for(int j=0;j<5;j++){
	Map<String, Object>    plantFactorMap_Fu = new HashMap<String, Object>(); 
	double  maxWallDamageFactor_Fu    = 0;
    double  maxWallFailConsequence_Fu = 0;
     
    for (Map<String, Object> WallPlantCorrosion : wallPlantCorrosion)  
    {  
//-------------------------------------����ڰ���������----------------------------------------
//      1���ڰ�����������
   	 double wallReductionDamageFactor_Fu = calculate.getReductionDamageFactor(
                
                    1,                                                             //����ڰ�
                    Double.parseDouble(WallPlantCorrosion.get("long_termCorrosion").toString()),            //�ò�ڰ帯ʴ����
                    useDate + 1 + j,                                                                                    //ʹ��ʱ��(����ʹ��ʱ��)
                    Double.parseDouble(WallPlantCorrosion.get("namelyThickness").toString()),               //�ڰ��ʼ���(Ŀǰ��������)

                    Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                      //�������
                    plantTest.get(0).get("wallCheckValidity").toString(),                                    //������Ч��
                    
                    plantInfo.get(0).get("wallboardLinkType").toString(),                                    //�ڰ�������ʽ
                    Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //�Ƿ񰴹涨ά��
                    Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //������������
               
            );
   	 
//      2���ڰ��ⲿ��������
         double wallOutDamageFactor_Fu  = calculate.getOutDamageFactor(
       		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //�����¶�
       		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //����¶�
                 1,                                                                                            //����ڰ�
                 Double.parseDouble(WallPlantCorrosion.get("namelyThickness").toString()),                    //�ڰ��ʼ���(Ŀǰ��������)

                 Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //�ܵ����Ӷ�
                 Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //�Ƿ��²㺬��
                 Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //�Ƿ��֧�ܲ���
                 Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //�Ƿ���油��
                 Integer.parseInt(plantMech.get(0).get("wallOutDamageMechanismId").toString()),                //�ⲿ���˻���
                
                 useDate + 1 + j,                                                                                        //ʹ��ʱ��(����ʹ��ʱ��)
                 Integer.parseInt(plantInfo.get(0).get("isWallboardKeepWarm").toString()),                                //�Ƿ��б��²�
                 Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),                            //���²�����
                 Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                             //Ϳ������
                 useDate + 1 + j, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),                         //Ϳ��ʹ��ʱ��
                 plantInfo.get(0).get("wallboardLinkType").toString(),                                       //������ʽ
                 
                Integer.parseInt(plantMech.get(0).get("wallSCCMechanismId").toString()),                   //SCC���˻���
                Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                            //�������
                plantTest.get(0).get("wallCheckValidity").toString(),                                           //������Ч��

                Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //�豸����
                Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //�Ƿ񰴹涨ά��
                Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //������������
                Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //�Ƿ���ʺ�ˮ
                
                Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //ˮ�е�H2Sֵ
                Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //ˮ��PHֵ
                Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //�Ƿ񺸺��ȴ���
                Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //�����Ӳ��
                Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //ˮ��cl���Ӻ���

                Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //�Ƿ����
                Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOHŨ��
                Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //�Ƿ�������ɨ
                Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //�Ƿ����Ӧ������
                Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //����ʷ
                Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //�Ƿ���ͣ������
                plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //����������
                Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //�ְ�������
                Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //̼����Ũ��
        );
   	 
//    3���ڰ�SCC��������

         double wallSCCDamageFactor_Fu = calculate.getSCCDamageFactor(
       		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //�����¶�
       		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //����¶�
       		  Integer.parseInt(plantMech.get(0).get("wallSCCMechanismId").toString()),            //SCC���˻���
       		  Integer.parseInt(plantTest.get(0).get("wallCheckTime").toString()),                 //�������
             plantTest.get(0).get("wallCheckValidity").toString(),                               //������Ч��
             0,                                                                                  //�����ⲿ�������ӵ�ʱ����Ҫ���������Ըߵͣ�ֱ���������س̶�ָ��
            
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //�Ƿ���ʺ�ˮ
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //ˮ�е����⺬��
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //ˮ��pHֵ
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //�Ƿ񺸺��ȴ���
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //�����Ӳ��
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //ˮ�е������Ӻ���

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //�Ƿ����
       		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOHŨ��
       		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //�Ƿ�������ɨ
       		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //�Ƿ����Ӧ������
       		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //����ʷ
       		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //�Ƿ�ͣ������
       		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //����������
       		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //�ְ�������
       		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //̼����Ũ��
             );       
            
//           4���ڰ���Զ�������
//             wallFactorMap.put("wallBrittleDamageFactor", 0);
          
           //------------------------------------������������-----------------------------------------------------------

//           ����õ��������ӵ�ֵ
      double  wallDamageFactor_Fu = calculate.getDamageFaction(
               0,                                     //��ʴ���ͣ��ڰ�Ϊ���ȸ�ʴ���װ�Ϊ�ֲ���ʴ 0������ȸ�ʴ 1����ֲ���ʴ
               wallReductionDamageFactor_Fu,     //������������
               wallOutDamageFactor_Fu,           //�ⲿ��������
               wallSCCDamageFactor_Fu,           //Ӧ����ʴ������������
               0             //���Զ�����������
           );
      
              
//------------------------------�ڰ�ʧЧ���----------------------------------------------------------
      double wallFailConsequence_Fu = calculate.getFailureWallConsequence(
   		   Double.parseDouble(plantInfo.get(0).get("D").toString()),                            //��ֱ��
   		   Double.parseDouble(WallPlantCorrosion.get("height").toString()),         //���޵���ڰ�߶�
   		   Integer.parseInt(WallPlantCorrosion.get("layerNO").toString()),           //�ڼ���ڰ�
           Integer.parseInt(plantInfo.get(0).get("sensitiveEnvironment").toString()),             //ʧЧ����ɽ��ܵĻ�׼ ��λ����Ԫ
   		   Double.parseDouble(plantInfo.get(0).get("fillH").toString()),                         //й¶���Ϸ���Һ��߶�
   		   Double.parseDouble(plantInfo.get(0).get("fMatCost_").toString()),                     //���ϼ۸�ϵ��
   		   Double.parseDouble(plantInfo.get(0).get("stopLoss").toString()),                      //ͣ����ɵ���ʧ
   		   Double.parseDouble(plantInfo.get(0).get("overflowPercentage").toString()),            //���Χ�ߵ�����ٷֱ�
   		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageIn").toString()),          //���Χ�������ڹ����ڣ��ر������е�����ٷֱ�
   		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageOut").toString())          //���Χ���������������⣬�ر������е�����ٷֱ�
          ); 
      
   	 
//--------------------------��ȡ������������ֵ����ֵ-------------------------------------------------
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
//-----------------------------�ڰ���������---------------------------------------------------------
//  �ҳ��ڰ������������ӣ���������������Ӽ�����յȼ�     
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
 
//�ڰ�ʧЧ������
double wallFailurePro_Fu        = calculate.getFailurePro(1, wallAverageFailurePro, maxWallDamageFactor_Fu);  //�ڰ�ʧЧ������
int    wallFailureProLevel_Fu   = 1;
//����ڰ�ʧЧ�����Եȼ�
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

//-----------------------------------------����װ���յȼ�--------------------------------------

double wallRisk_Fu      = maxWallFailConsequence_Fu * wallFailurePro_Fu;                              //�ڰ����
String wallRiskLevel_Fu = riskMatrixTable[wallFailureProLevel_Fu][wallFailConsequenceLevelIndex_Fu];  //�ڰ���յȼ�

plantFactorMap_Fu.put("wallRisk_Fu", wallRisk_Fu);
plantFactorMap_Fu.put("wallRiskLevel_Fu", wallRiskLevel_Fu);

//-------------------------�װ���ռ���ָ���-----------------------------------------------------


double  maxBottomEdgeDamageFactor_Fu        = 0;
double  maxBottomEdgeFailConsequence_Fu     = 0;
String  maxBottomEdgeValueLayerId_Fu        = "";

for (Map<String, Object>  bottomEdgeCorrosion : bottomEdgePlantCorrosion)  
{  
//-------------------------------------��Ե�����----------------------------------------
//  1����Ե�����������
	 double bottomEdgeReductionDamageFactor_Fu = calculate.getReductionDamageFactor(
            
                1,                                                                                       //����ڰ�
                Double.parseDouble(bottomEdgeCorrosion.get("long_termCorrosion").toString()),            //�ò�ڰ帯ʴ����
                useDate + 1 + j,                                                                                     //ʹ��ʱ��(����ʹ��ʱ��)
                Double.parseDouble(plantInfo.get(0).get("bottomEdgeNamelyThickness").toString()),               //�ڰ��ʼ���(Ŀǰ��������)

                Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                      //�������
                plantTest.get(0).get("bottomCheckValidity").toString(),                                    //������Ч��
                
                plantInfo.get(0).get("bottomLinkType").toString(),                                    //�ڰ�������ʽ
                Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //�Ƿ񰴹涨ά��
                Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //������������
           
        );
	 
//  2����Ե���ⲿ��������
     double bottomEdgeOutDamageFactor_Fu  = calculate.getOutDamageFactor(
	   		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //�����¶�
	   		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //����¶�
             1,                       //����ڰ�
             Double.parseDouble(plantInfo.get(0).get("bottomEdgeNamelyThickness").toString()),                    //�ڰ��ʼ���(Ŀǰ��������)

             Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //�ܵ����Ӷ�
             Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //�Ƿ��²㺬��
             Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //�Ƿ��֧�ܲ���
             Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //�Ƿ���油��
             Integer.parseInt(plantMech.get(0).get("floorOutDamageMechanismId").toString()),                //�ⲿ���˻���
            
             useDate + 1 + j,                                                                                        //ʹ��ʱ��(����ʹ��ʱ��)
             0,                                                                                          //�Ƿ��б��²㣨�װ�û�б��²㣩
             Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),               //���²�����
             Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                //Ϳ������
             useDate + 1 + j, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),               //Ϳ��ʹ��ʱ��
             plantInfo.get(0).get("bottomLinkType").toString(),                                       //������ʽ
             
            Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),                   //SCC���˻���
            Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                            //�������
            plantTest.get(0).get("bottomCheckValidity").toString(),                                           //������Ч��

            Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //�豸����
            Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //�Ƿ񰴹涨ά��
            Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //������������
            Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //�Ƿ���ʺ�ˮ
            
            Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //ˮ�е�H2Sֵ
            Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //ˮ��PHֵ
            Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //�Ƿ񺸺��ȴ���
            Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //�����Ӳ��
            Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //ˮ��cl���Ӻ���

            Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //�Ƿ����
            Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOHŨ��
            Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //�Ƿ�������ɨ
            Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //�Ƿ����Ӧ������
            Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //����ʷ
            Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //�Ƿ���ͣ������
            plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //����������
            Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //�ְ�������
            Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //̼����Ũ��
    );
	 
//3����Ե��SCC��������

     double bottomEdgeSCCDamageFactor_Fu = calculate.getSCCDamageFactor(
   		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //�����¶�
   		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //����¶�
   		  Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),            //SCC���˻���
   		  Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                 //�������
             plantTest.get(0).get("bottomCheckValidity").toString(),                               //������Ч��
             0,                                                                                  //�����ⲿ�������ӵ�ʱ����Ҫ���������Ըߵͣ�ֱ���������س̶�ָ��
            
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //�Ƿ���ʺ�ˮ
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //ˮ�е����⺬��
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //ˮ��pHֵ
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //�Ƿ񺸺��ȴ���
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //�����Ӳ��
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //ˮ�е������Ӻ���

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //�Ƿ����
   		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOHŨ��
   		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //�Ƿ�������ɨ
   		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //�Ƿ����Ӧ������
   		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //����ʷ
   		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //�Ƿ�ͣ������
   		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //����������
   		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //�ְ�������
   		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //̼����Ũ��
         );
        
//       4����Ե����Զ�������
//         bottomEdgeFactorMap.put("bottomEdgeBrittleDamageFactor", 0);
      
       //------------------------------------������������-----------------------------------------------------------

//       ����õ��������ӵ�ֵ
  double  bottomEdgeDamageFactor_Fu = calculate.getDamageFaction(
           1,                                     //��ʴ���ͣ��ڰ�Ϊ���ȸ�ʴ���װ�Ϊ�ֲ���ʴ 0������ȸ�ʴ 1����ֲ���ʴ
           bottomEdgeReductionDamageFactor_Fu,     //������������
           bottomEdgeOutDamageFactor_Fu,           //�ⲿ��������
           bottomEdgeSCCDamageFactor_Fu,           //Ӧ����ʴ������������
           0             //���Զ�����������
       );     

	 
	 //��ȡ������������ֵ����ֵ
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
//-------------------------------------�м�����----------------------------------------
//  1���м�����������
	 double bottomMiddleReductionDamageFactor_Fu = calculate.getReductionDamageFactor(
            
                1,                                                                                       //����ڰ�
                Double.parseDouble(bottomMiddleCorrosion.get("long_termCorrosion").toString()),            //�ò�ڰ帯ʴ����
                useDate + 1 + j,                                                                                     //ʹ��ʱ��(����ʹ��ʱ��)
                Double.parseDouble(plantInfo.get(0).get("bottomMiddleNamelyThickness").toString()),               //�ڰ��ʼ���(Ŀǰ��������)

                Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                      //�������
                plantTest.get(0).get("bottomCheckValidity").toString(),                                    //������Ч��
                
                plantInfo.get(0).get("bottomLinkType").toString(),                                    //�ڰ�������ʽ
                Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                            //�Ƿ񰴹涨ά��
                Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString())            //������������
           
        );
	 
//  2����Ե���ⲿ��������
     double bottomMiddleOutDamageFactor_Fu  = calculate.getOutDamageFactor(
   		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                         //�����¶�
   		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                          //����¶�
             1,                       //����ڰ�
             Double.parseDouble(plantInfo.get(0).get("bottomMiddleNamelyThickness").toString()),                    //�ڰ��ʼ���(Ŀǰ��������)

             Integer.parseInt(plantMech.get(0).get("pipeComplexity").toString()),                         //�ܵ����Ӷ�
             Integer.parseInt(plantMech.get(0).get("isKeepWarmHasCL").toString()),                         //�Ƿ��²㺬��
             Integer.parseInt(plantMech.get(0).get("outDamageisPipeSupport").toString()),                  //�Ƿ��֧�ܲ���
             Integer.parseInt(plantMech.get(0).get("outDamageisInterfaceCompensation").toString()),        //�Ƿ���油��
             Integer.parseInt(plantMech.get(0).get("floorOutDamageMechanismId").toString()),                //�ⲿ���˻���
            
             useDate + 1 + j,                                                                                        //ʹ��ʱ��(����ʹ��ʱ��)
             0,                                                                                          //�Ƿ��б��²㣨�װ�û�б��²㣩
             Integer.parseInt(plantInfo.get(0).get("wallboardKeepWarmStatus").toString()),               //���²�����
             Integer.parseInt(plantInfo.get(0).get("wallboardCoatingStatus").toString()),                //Ϳ������
             useDate + 1 +j, //Double.parseDouble(plantInfo.get(0).get("coatingUseDate").toString()),               //Ϳ��ʹ��ʱ��
             plantInfo.get(0).get("bottomLinkType").toString(),                                       //������ʽ
             
            Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),                   //SCC���˻���
            Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                            //�������
            plantTest.get(0).get("bottomCheckValidity").toString(),                                           //������Ч��

            Integer.parseInt(plantInfo.get(0).get("geographyEnvironment").toString()),                      //�豸����
            Integer.parseInt(plantInfo.get(0).get("isMaintainAsRequired").toString()),                      //�Ƿ񰴹涨ά��
            Integer.parseInt(plantInfo.get(0).get("tankFoundationSettlement").toString()),                  //������������
            Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                           //�Ƿ���ʺ�ˮ
            
            Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                          //ˮ�е�H2Sֵ
            Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                           //ˮ��PHֵ
            Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),                    //�Ƿ񺸺��ȴ���
            Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                         //�����Ӳ��
            Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),                      //ˮ��cl���Ӻ���

            Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                           //�Ƿ����
            Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),                 //NAOHŨ��
            Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                          //�Ƿ�������ɨ
            Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                          //�Ƿ����Ӧ������
            Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                          //����ʷ
            Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),                    //�Ƿ���ͣ������
            plantMech.get(0).get("SCCSurroundingsMedium").toString(),                //����������
            Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),                       //�ְ�������
            Double.parseDouble(plantMech.get(0).get("SCCWaterCarbonateConcentration").toString())        //̼����Ũ��
    );
	 
//3���ڰ�SCC��������

     double bottomMiddleSCCDamageFactor_Fu = calculate.getSCCDamageFactor(
   		  Double.parseDouble(plantInfo.get(0).get("operateTemp").toString()),                   //�����¶�
   		  Double.parseDouble(plantInfo.get(0).get("designTemp").toString()),                    //����¶�
   		  Integer.parseInt(plantMech.get(0).get("floorSCCMechanismId").toString()),            //SCC���˻���
   		  Integer.parseInt(plantTest.get(0).get("bottomCheckTime").toString()),                 //�������
             plantTest.get(0).get("bottomCheckValidity").toString(),                               //������Ч��
             0,                                                                                  //�����ⲿ�������ӵ�ʱ����Ҫ���������Ըߵͣ�ֱ���������س̶�ָ��
            
             Integer.parseInt(plantMech.get(0).get("isMediumWater").toString()),                  //�Ƿ���ʺ�ˮ
             Double.parseDouble(plantMech.get(0).get("SCCWaterH2S").toString()),                 //ˮ�е����⺬��
             Double.parseDouble(plantMech.get(0).get("SCCWaterpH").toString()),                  //ˮ��pHֵ
             Integer.parseInt(plantInfo.get(0).get("isHeatTreatAfterWeld").toString()),           //�Ƿ񺸺��ȴ���
             Double.parseDouble(plantMech.get(0).get("SCCBHardness").toString()),                //�����Ӳ��
             Double.parseDouble(plantMech.get(0).get("ClConcentration").toString()),             //ˮ�е������Ӻ���

             Integer.parseInt(plantMech.get(0).get("SCCisHeatTracing").toString()),                  //�Ƿ����
   		  Double.parseDouble(plantMech.get(0).get("SCCNaOHConcentration").toString()),        //NaOHŨ��
   		  Integer.parseInt(plantMech.get(0).get("SCCisSteamBlowing").toString()),                 //�Ƿ�������ɨ
   		  Integer.parseInt(plantMech.get(0).get("isStressRelief").toString()),                 //�Ƿ����Ӧ������
   		  Integer.parseInt(plantMech.get(0).get("SCCHeatHistory").toString()),                 //����ʷ
   		  Integer.parseInt(plantMech.get(0).get("SCCisShutdownProtect").toString()),           //�Ƿ�ͣ������
   		  plantMech.get(0).get("SCCSurroundingsMedium").toString(),       //����������
   		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString()),              //�ְ�������
   		  Double.parseDouble(plantMech.get(0).get("SCCSteelSulfur").toString())       //̼����Ũ��
         );
        
//       4���ڰ���Զ�������
//         bottomMiddleFactorMap.put("bottomMiddleBrittleDamageFactor", 0);
      
       //------------------------------------������������-----------------------------------------------------------

//       ����õ��������ӵ�ֵ
  double  bottomMiddleDamageFactor_Fu = calculate.getDamageFaction(
           1,                                     //��ʴ���ͣ��ڰ�Ϊ���ȸ�ʴ���װ�Ϊ�ֲ���ʴ 0������ȸ�ʴ 1����ֲ���ʴ
           bottomMiddleReductionDamageFactor_Fu,     //������������
           bottomMiddleOutDamageFactor_Fu,           //�ⲿ��������
           bottomMiddleSCCDamageFactor_Fu,           //Ӧ����ʴ������������
           0             //���Զ�����������
       );
          
	 
	 //��ȡ������������ֵ����ֵ
	 if(bottomMiddleDamageFactor_Fu > maxBottomEdgeDamageFactor_Fu ){
		 maxBottomMiddleDamageFactor_Fu        = bottomMiddleDamageFactor_Fu;
	 } else {
		 maxBottomMiddleDamageFactor_Fu        = bottomMiddleDamageFactor_Fu;
	 }
} 

//----------------------------�װ���������------------------------------------------------------

	 double floorDamageFactor_Fu =  0;
	 
	 if(maxBottomEdgeDamageFactor_Fu >= maxBottomMiddleDamageFactor_Fu){
	      floorDamageFactor_Fu =  maxBottomEdgeDamageFactor_Fu;
	 }else{
	      floorDamageFactor_Fu =  maxBottomMiddleDamageFactor_Fu;
	 }
    
//---------------------------------�װ�ʧЧ���--------------------------------------
	 double floorFailConsequence_Fu = calculate.getFailurefloorConsequence(
		   Double.parseDouble(plantInfo.get(0).get("D").toString()),                          //����ֱ��
		   Double.parseDouble(plantInfo.get(0).get("fillH").toString()),                      //����Һ��߶�
		   Integer.parseInt(plantInfo.get(0).get("sensitiveEnvironment").toString()),       //�������ж�
		   Double.parseDouble(plantInfo.get(0).get("fMatCost_").toString()),                  //���ϼ۸�ϵ��
		   Double.parseDouble(plantInfo.get(0).get("stopLoss").toString()),                   //ͣ����ɵ���ʧ
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentage").toString()),         //���Χ��������ٷֱ�
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageIn").toString()),       //���Χ�������ڹ����ڣ��ر������е�����ٷֱ�
		   Double.parseDouble(plantInfo.get(0).get("overflowPercentageOut").toString()),      //���Χ���������������⣬�ر������е�����ٷֱ�
	       Double.parseDouble(plantInfo.get(0).get("bottomToWaterDistance").toString()),      //�޵׵�����ˮ�ľ���
	   	   Double.parseDouble(plantInfo.get(0).get("mediumPercentage").toString()),           //�����ܶ�
		   Double.parseDouble(plantInfo.get(0).get("mediumDyViscosity").toString()),          //���ʶ���ճ��
		   Integer.parseInt(plantInfo.get(0).get("bottomGasket").toString()),               //���޻�����ʽ��0---����Ϊˮ�������  1������������RPB��2��������û��RPB
		   Integer.parseInt(plantInfo.get(0).get("bottomGasketSoil").toString())           //���޻���������������
      ); 

	plantFactorMap_Fu.put("floorDamageFactor_Fu", floorDamageFactor_Fu);
	plantFactorMap_Fu.put("floorFailConsequence_Fu", floorFailConsequence_Fu);
	
	//�ҳ��ڰ������������ӣ���������������Ӽ�����յȼ�     
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
	
	//�ڰ�ʧЧ������
	double floorFailurePro_Fu        = calculate.getFailurePro(1, floorAverageFailurePro, floorDamageFactor_Fu);  //�ڰ�ʧЧ������
	int    floorFailureProLevel_Fu   = 1;
	//����ڰ�ʧЧ�����Եȼ�
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
	
	//------------------------------------------------�װ���յȼ�---------------------------------------------------------
	
	double floorRisk_Fu      = floorFailConsequence_Fu*floorFailurePro_Fu;
	String floorRiskLevel_Fu = riskMatrixTable[floorFailureProLevel_Fu][floorFailConsequenceLevelIndex_Fu];
	
	plantFactorMap_Fu.put("floorRisk_Fu", floorRisk_Fu);
	plantFactorMap_Fu.put("floorRiskLevel_Fu", floorRiskLevel_Fu);
	plantFactor_Fu.add(plantFactorMap_Fu);
}


//-------------------------------�����´μ���ʱ��---------------------------------
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


//-------------------------------------���ս������--------------------------------------
//-------------------------------------���ս������--------------------------------------
//-------------------------------------���ս������--------------------------------------
	
	
	try {  
        Class.forName(name);//ָ����������  
        conn = DriverManager.getConnection(url, user, password);  //��ȡ����  
        stmt = conn.createStatement();                            //����Statement����
        
        //��ѯ���豸�����б��Ƿ��Ѵ���
        ResultSet rs1 = stmt.executeQuery("select * from tb_riskrecordlist where pid="+PlantId);//�������ݶ���
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        int re2 = 0;
        String  wallEvaDate = df.format(new Date());
        String floorEvaDate = df.format(new Date());
        if(rs1.next()){   //�����������Ϊ��
            //ԭ���Ѿ��м�¼�������
        	  re2 = stmt.executeUpdate("update tb_riskrecordlist set wallEvaDate='"
            +wallEvaDate+"', floorEvaDate='"+floorEvaDate+"' where pid="+PlantId);//�������ݶ���             	
        }else{
        	//ԭ��û�м�¼��������
         	  re2 = stmt.executeUpdate("insert into tb_riskrecordlist set pid="+PlantId
         			 +",wallEvaDate='"+wallEvaDate+"', floorEvaDate='"+floorEvaDate+"'");//�������ݶ���
        }
        
//        //��ѯ�豸��صķ��ռ�¼��idֵ
//        int riskRecordListId = 0;
//        if(re2>0){
//        	ResultSet rs2 = stmt.executeQuery("select id from tb_riskrecordlist where pid="+PlantId);//�������ݶ���
//        	while(rs2.next()){
//        		riskRecordListId = rs2.getInt("id");
//        	}
//        	rs2.close();
//        } 
        
        //������ռ�¼�б�
        PreparedStatement pstmt = conn.prepareStatement("insert into tb_risklist set "
        		+ "pid="+PlantId+","                       //�豸Id
        		+ "factoryId='"+plantInfo.get(0).get("factoryId").toString()+"',"           //��������  
        		+ "workshopId='"+plantInfo.get(0).get("workshopId").toString()+"',"          //��������
        		+ "areaId='"+plantInfo.get(0).get("areaId").toString()+"',"              //��������
        		+ "plantNO='"+plantInfo.get(0).get("plantNO").toString()+"',"             //�豸λ��
        		+ "plantName='"+plantInfo.get(0).get("plantName").toString()+"',"            //�豸����
        		+ "countDate='"+df.format(new Date())+"',"            //����ʱ��
        		
        		+ "wallMajorRisk='"+maxValueLayerId+"',"            //�ڰ���������
        		+ "floorMajorRisk='"+floorMajorRisk+"',"            //floorMajorRisk
        		+ "wallDamageFactor='"+maxWallDamageFactor+"',"            //wallDamageFactor
        		+ "floorDamageFactor='"+floorDamageFactor+"',"            //floorDamageFactor
        		
        		+ "floorEdgeDamageFactor='"+maxBottomEdgeDamageFactor+"',"            //�װ��Ե�������������ֵ
        		+ "floorEdgeCorrosionSpeed='"+maxBottomEdgeCorrosion+"',"            //�װ��Ե�������������ֵ��Ӧ�ĸ�ʴ����
        		+ "floorMiddleDamageFactor='"+maxBottomMiddleDamageFactor+"',"            //�װ��м�������������ֵ
        		+ "floorMiddleCorrosionSpeed='"+maxBottomMiddleCorrosion+"',"            //�װ��м�������������ֵ��Ӧ�ĸ�ʴ����
        		
        		+ "wallRisk='"+wallRisk+"',"            //�ڰ����
        		+ "wallRiskLevel='"+wallRiskLevel+"',"            //�ڰ���յȼ�
        		+ "floorRisk='"+floorRisk+"',"            //�װ����
        		+ "floorRiskLevel='"+floorRiskLevel+"',"            //�װ���յȼ�
        		
        		+ "wallFailPro='"+wallFailurePro+"',"            //�ڰ�ʧЧ������
        		+ "wallFailProLevel='"+wallFailureProLevel+"',"            //�ڰ�ʧЧ�����Եȼ�
        		+ "floorFailPro='"+floorFailurePro+"',"            //�װ�ʧЧ������
        		+ "floorFailProLevel='"+floorFailureProLevel+"',"            //�װ�ʧЧ�����Եȼ�
        		+ "wallConsequence='"+maxWallFailConsequence+"',"            //�ڰ�ʧЧ���
        		+ "floorConsequence='"+floorFailConsequence+"',"            //�װ�ʧЧ���
        		+ "wallConsequenceLevel='"+wallFailConsequenceLevel+"',"            //�װ�ʧЧ����ȼ�
        		+ "floorConsequenceLevel='"+floorFailConsequenceLevel+"',"            //�װ�ʧЧ����ȼ�
        		
        		+ "wallFailPro_fu="+Double.parseDouble(plantFactor_Fu.get(4).get("wallFailurePro_Fu").toString())+","            //�ڰ�δ��ʧЧ������
        		+ "floorFailPro_fu="+Double.parseDouble(plantFactor_Fu.get(4).get("floorFailurePro_Fu").toString())+","            //�װ�δ��ʧЧ������
        		+ "wallRisk_fu="+Double.parseDouble(plantFactor_Fu.get(4).get("wallRisk_Fu").toString())+","            //�ڰ�δ������
        		+ "floorRisk_fu="+Double.parseDouble(plantFactor_Fu.get(4).get("floorRisk_Fu").toString())+","            //�װ�δ������
        		+ "wallRiskLevel_fu='"+plantFactor_Fu.get(4).get("wallRiskLevel_Fu").toString()+"',"            //�ڰ�δ�����յȼ�
        		+ "floorRiskLevel_fu='"+plantFactor_Fu.get(4).get("floorRiskLevel_Fu").toString()+"',"            //�װ�δ�����յȼ�
        		
        		+ "wallNextCheckDate='"+wallNextCheckDate+"',"            //�ڰ�δ�����յȼ�
        		+ "floorNextCheckDate='"+floorNextCheckDate+"',"            //�װ�δ�����յȼ�
        		
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
        		+plantFactor_Fu.get(4).get("maxWallDamageFactor_Fu").toString()+"',"            //�ڰ�������������
        		
        		+ "floorDamageFactor_trend='"
        		+plantFactor_Fu.get(0).get("floorDamageFactor_Fu").toString()+","
        		+plantFactor_Fu.get(1).get("floorDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(2).get("floorDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(3).get("floorDamageFactor_Fu").toString()+"," 
        		+plantFactor_Fu.get(4).get("floorDamageFactor_Fu").toString()+"'"            //�װ�������������
        		
        		,Statement.RETURN_GENERATED_KEYS);
        
        pstmt.executeUpdate();                    //ִ��
        ResultSet rs3  = pstmt.getGeneratedKeys(); //��ȡ���   
        int risklistId = 0;
        if (rs3.next()) {  
        	risklistId = rs3.getInt(1);
        }
        rs3.close();
        pstmt.close();
        
        //����ڰ���ռ�����
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
    			+",damageFactor="+Double.parseDouble(WallFactor.get("wallDamageFactor").toString())+"");//�������ݶ��� 
       	  i++;
        }       
        int re4 = 0; 
        if (   wallRiskLevel    ==  "�߷���"
        	|| wallRiskLevel    ==  "�и߷���"
        	|| floorRiskLevel   ==  "�߷���"
        	|| floorRiskLevel   ==  "�и߷���"
        ) {
            re4 = stmt.executeUpdate("update tb_risklist set isAlarm=1 where id="+risklistId);//�������ݶ���
        }
        
        //�������ݶ���
        if(re4>0){
        	re[0] = "�������ɹ�";
        	re[1] = ""+1;
        }else{
        	re[0] = "����������";
        	re[1] = ""+0;
        }
        
        rs1.close();
        stmt.close();
        conn.close();
       
        
    } catch (Exception e) {  
        e.printStackTrace();  
    }  

//-----------------------����ר�÷ָ���--------------------------------------------------------------------------
     
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
     
//	 System.out.println("����" + list);
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
	
//   ��ȡ���ռ���Ļ���
	public List<Map<String,Object>> getPlantInfo(int plantId){
		sql = "select * from tb_plantinfo AS PlantInfo LEFT JOIN "
				+ "tb_plantwallboardinfo  AS PlantWallboardInfo "
				+ "ON PlantInfo.id = PlantWallboardInfo.pid "
				+ "where PlantInfo.id=" + plantId;//SQL���  
	
        db1 = new DBHelper(sql);//����DBHelper����  
        int                i = 0;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
        try {  
            ret = db1.pst.executeQuery();//ִ����䣬�õ������  
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
            db1.close();//�ر����� 
        } catch (SQLException e) { 
            e.printStackTrace(); 
            
        }
        return list;  
         
	}    
	
	//��ȡ�豸�ıڰ�����
	public List<Map<String,Object>> getWallboardInfo(int plantId){
		sql = "select * from tb_plantwallboardinfo  where pid=" + plantId;//SQL���  
	
        db1 = new DBHelper(sql);//����DBHelper����  
        int                i = 0;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
        try {  
            ret = db1.pst.executeQuery();//ִ����䣬�õ������  
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
            db1.close();//�ر����� 
        } catch (SQLException e) { 
            e.printStackTrace(); 
            
        }
        return list;  
	}
	    
	public List<Map<String,Object>> getPlantMech(int plantId){
		sql = "select * from tb_riskcalpara  where pid=" + plantId;//SQL���  
	
        db1 = new DBHelper(sql);//����DBHelper����  
        int                i = 0;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
        try {  
            ret = db1.pst.executeQuery();//ִ����䣬�õ������  
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
            db1.close();//�ر����� 
        } catch (SQLException e) { 
            e.printStackTrace(); 
            
        }
        return list;  
	}

	//��ȡ�豸�ļ�������ͼ�����Ч��
		public List<Map<String,Object>> getPlantTest(int plantId){
			sql = "select * from tb_planttestrecord as PlantTestRecord left join "
					+ "(select * from tb_planttestrecordwall group by pid) as PlantTestRecordWall "
					+ "on PlantTestRecord.id = PlantTestRecordWall.pid where PlantTestRecord.pid=" + plantId;//SQL���  
		
	        db1 = new DBHelper(sql);//����DBHelper����  
	        int                i = 0;
	        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
	        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>(); 
	        try {  
	            ret = db1.pst.executeQuery();//ִ����䣬�õ������  
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
	        db1.close();//�ر����� 
	        double    wallCheckTime       = 0;
	        double    bottomCheckTime     = 0;
	        double    sum                 = 0;
	        String    testMethod_wall   = null;
	        String    testMethod_bottom   = null;
	        for (Map<String, Object> map: list)
		    {
	        	testMethod_wall = map.get("testMethod").toString();
		    	  switch(testMethod_wall){
	                case "�߶���Ч" :
	                    sum=1;
	                    break;
	                case "�и߶���Ч" :
	                    sum=0.5;
	                    break;
	                case "�ж���Ч" :
	                    sum=0.25;
	                    break;
	                case "�Ͷ���Ч" :
	                    sum=0.125;
	                    break;
	                case "��Ч" :
	                    sum=0;
	                    break;
	            }
		    	  wallCheckTime=wallCheckTime+sum;
	            
	            testMethod_bottom = map.get("testMethod_bottom").toString();
	            switch(testMethod_bottom){
	                case "�߶���Ч" :
	                    sum=1;
	                    break;
	                case "�и߶���Ч" :
	                    sum=0.5;
	                    break;
	                case "�ж���Ч" :
	                    sum=0.25;
	                    break;
	                case "�Ͷ���Ч" :
	                    sum=0.125;
	                    break;
	                case "��Ч" :
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
		
//��ȡ��ʴ������ز���		
		public List<Map<String,Object>> getWallPlantCorrosion(int plantId){
			
			sql = "select max(Mea_dt) from tb_measurethicknessrecord_wall "
					+ "where part = 1 and gpid=" + plantId;//SQL���  
	        db1 = new DBHelper(sql);//����DBHelper����  
	        
	        String max_Meadt = null;
	        try {  
	            ret = db1.pst.executeQuery();//ִ����䣬�õ������  
	            while(ret.next()){  
	            	max_Meadt = ret.getString(1);   
	            }  
	        ret.close();  
	        db1.close();//�ر�����    
	        } catch (SQLException e) { 
	            e.printStackTrace(); 
	            
	        }
	        
	        sql = "select * from (tb_measurethicknessrecord_wall as TMW left join tb_plantwallboardinfo "
	        		+ "as TMI on TMW.layerGpid=TMI.id) left join tb_measurethicknessrecord_wall_origin "
	        		+ "as TMO on TMW.layerPid=TMO.id where TMW.part = 1 and TMW.gpid=" + plantId + 
	        		" and TMW.Mea_dt = '"+ max_Meadt +"'";  
		
	        db1 = new DBHelper(sql);//����DBHelper����  
	        int                i = 0;
	        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
//	        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>(); 
	        try {  
	            ret = db1.pst.executeQuery();//ִ����䣬�õ������  
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
	            db1.close();//�ر����� 
	        } catch (SQLException e) { 
	            e.printStackTrace(); 
	            
	        }
	        
	        return list;  
		}
		
		//��ȡ�װ帯ʴ������ز���		
				public List<Map<String,Object>> getBottomPlantCorrosion(int plantId, int part, int layerNO){
					
					sql = "select max(Mea_dt) from tb_measurethicknessrecord_wall "
							+ "where part = "+part+" and gpid=" + plantId +
							" and layerNO =" + layerNO;//SQL���  
			        db1 = new DBHelper(sql);//����DBHelper����  
			        
			        String max_Meadt = null;
			        try {  
			            ret = db1.pst.executeQuery();//ִ����䣬�õ������  
			            while(ret.next()){  
			            	max_Meadt = ret.getString(1);   
			            }  
			        ret.close();  
			        db1.close();//�ر�����    
			        } catch (SQLException e) { 
			            e.printStackTrace(); 
			            
			        }
			        
			        sql = "select * from (tb_measurethicknessrecord_wall as TMW left join tb_plantwallboardinfo "
			        		+ "as TMI on TMW.layerGpid=TMI.id) left join tb_measurethicknessrecord_wall_origin "
			        		+ "as TMO on TMW.layerPid=TMO.id where TMW.part = "+part+" and TMW.gpid=" + plantId + 
			        		" and TMW.Mea_dt = '"+ max_Meadt +"'"+ "and TMW.layerNO =" + layerNO;  
				
			        db1 = new DBHelper(sql);//����DBHelper����  
			        int                i = 0;
			        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
//			        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>(); 
			        try {  
			            ret = db1.pst.executeQuery();//ִ����䣬�õ������  
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
			            db1.close();//�ر����� 
			        } catch (SQLException e) { 
			            e.printStackTrace(); 
			            
			        }
			        
			        return list;  
				}

	//����ʧЧ�����ԣ�Fg ƽ��ʧЧ������   Fm ����������ϵ��   Fe ��������
	//���ߵĳ˻�����ʧЧ������
	public double getFailurePro(double Fg,double Fm,double Fe){
		double failPro = Fg*Fm*Fe;
		return failPro;
	}
	
	/*����ƽ��ʧЧ����
	 * 1��part����ڰ���ߵװ� 0����װ� 1����ڰ�
	 * 2��break��й©�׳ߴ�
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
	  //���ʲ�����Ϊ0������ı�׼Ӧ�������⣬������Ϊ0�ĸ�Ϊ0.00001
	  //���������GB22610�ı�׼
		double[][] averageFailProArr = {
				{0.00072, 0.00001, 0.00001, 0.00002},
				{0.00007, 0.000025, 0.000005, 0.0000007}
		};
		res = averageFailProArr[part][breakSizeIndex];
		return res;
	}
	
//--------------------------------------------------------------------------------------------
//������������
	/* 1��corrosionType           ��ʴ���ͣ��ֲ���ʴ���Ǿ��ȸ�ʴ  0��ʾ���ȸ�ʴ   1��ʾ�ֲ���ʴ
	 * 2��reductionDamageFaction  ������������
	 * 3��outDamageFaction        �ⲿ��������
	 * 4��SCCDamageFaction        Ӧ����ʴ������������
	 * 5��brittleFractureFaction  ���Զ�����������
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
	
//���������������
	/* 1��part                       //��ʾ�ڰ���ߵװ� 0����װ�   1����ڰ�
	 * 2��corrosionSpeed             //��ʴ����
	 * 3��useDate                    //ʹ������
	 * 4��thickness                  //���
	 * 5��checkTime                  //�������
	 * 6��checkValidity             //������Ч��
	 * 7��linkType                   //������ʽ
	 * 8��isMaintenanceAsRequired    //�Ƿ���Ҫ�����ά��  0�����  1������
	 * 9��tankFoundationSettlement   //������������
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
       
		double   severity                 = 0;             //���س̶�ָ��
		double   reductionDamageFactor    = 1;             //������������
		
		String[] checkConditionIndexArr   = {              //��������ͼ�����Ч�Լ������硰1A������4D��
				"0", "1A", "1B", "1C", "1D", 
				"2A", "2B", "2C", "2D", 
				"3A", "3B", "3C", "3D", 
				"4A", "4B", "4C", "4D", 
				"5A", "5B", "5C", "5D",
				"6A", "6B", "6C", "6D"	
		};
		
		double[] severityIndexArr         = {              //���س̶�ָ��������
				0.02, 0.04, 0.06, 0.08, 0.10, 0.12, 
				0.14, 0.16, 0.18, 0.20, 0.25, 0.30, 
				0.35, 0.40, 0.45, 0.50, 0.55, 0.60, 
				0.65
		};
		
		 //    �ڰ���������ӵı�׼������GB22610.4��C.7
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
//      �װ���������ӵı�׼������GB22610.4��C.7
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

//		�������س̶�ָ��
		severity = (double)(Math.round(thickness * corrosionSpeed / useDate*100)/100.0);
//      �����������ͼ�����Ч�Ե�
		String   checkCondition           = "0";  //��������ͼ�����Ч������ֵ���硰1A������4D��
		if(checkTime == 0){
			checkCondition = "0";
		}else if(checkTime > 0 && checkTime <= 6){
			checkCondition = checkTime+checkValidity;
		}else if(checkTime > 6){
			checkCondition = "6"+checkValidity;
		}
//	    �������س̶�ָ������ֵ
		int severityIndex  =  0;
		for (int i = 1; i < severityIndexArr.length; i++) { 			 
			 double a             = Math.abs(severityIndexArr[0] - severity);
			 double b             = Math.abs(severityIndexArr[i] - severity);
			 if(b<a){
			    severityIndex  =  i;
				a              = Math.abs(severityIndexArr[i] - severity);
			 }
		}
//	    �����������ͼ�����Ч������ֵ
		int checkConditionIndex  =  0;	
		for (int i = 0; i < checkConditionIndexArr.length; i++) {  
            if (checkConditionIndexArr[i].equals(checkCondition)) {  
            	checkConditionIndex  =  i;
            }  
        }
//    �������������
		 if(part==1){
			 reductionDamageFactor = reductionWallDamageFactorTable[severityIndex][checkConditionIndex];
	        }else if(part==0){
	         reductionDamageFactor = reductionFloorDamageFactorTable[severityIndex][checkConditionIndex];
	        }
		 //����ϵ����Ӱ��
		 int fWD = 1;
		    if(linkType.equals("����")){
	            fWD=1;
	        }else{
	            fWD=10;
	        }
	     //����ά������ϵ�� 1�����ǣ�0������Ƿ���Ҫ��ά��
		 double fAM = 1;
	        if(isMaintenanceAsRequired==1){
	            fAM=1;
	        }else{
	            fAM=5;
	        }
	     //�����������۵ȼ���Ӱ��
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
	
//------------------------------SCCӦ����ʴ������������--------------------------------------------------------------------
/* 1�����  
 * 2��������  
 * 3������Ӧ����ʴ���ѣ�SSCC�� 
 * 4��̼���θ�ʴ����  
 * 5���������Ὺ�ѣ�PTA��
 * 6���Ȼ���Ӧ����ʴ���ѣ�CISCC�� 
 * 7����Ӧ�����ѣ�HSC-HF��HIC/SOHIC_HF��
 * */
  public double getSCCDamageFactor(
		double operatingTemp,                  //�����¶�
		double designTemp,                     //����¶�
		int    SCCMechId,                      //SCC���˻���
		int    checkTime,                      //�������
		String checkValidity,                  //������Ч��
		int    severityPara,                   //�����ⲿ�������ӵ�ʱ����Ҫ���������Ըߵͣ�ֱ���������س̶�ָ��
		int    isMediumWater,                  //�Ƿ���ʺ�ˮ
		double SCCWaterH2S,                    //ˮ�е����⺬��
		double SCCWaterpH,                     //ˮ��pHֵ
		int    isHeatTreatAfterWeld,            //�Ƿ񺸺��ȴ���
		double SCCBHardness,                   //�����Ӳ��
		double clConcentration,                //ˮ�е������Ӻ���
		
		int    isHeatTreacing,                 //�Ƿ����
		double NaOHConcentration,               //NaOHŨ��
		int    isSteamBlowing,                 //�Ƿ�������ɨ
		int    isStressRelief,                  //�Ƿ����Ӧ������
		int    SCCHeatHistory,                  //����ʷ
		int    SCCisShutdownProtect,            //�Ƿ�ͣ������
		String surrounding,                     //����������
		double SCCSteelSulfur,                  //�ְ�������
		double SCCWaterCarbonateConcentration   //̼����Ũ��
		   ){
//	   ��������Ϊ0.����Ϊ�͡��С��ߣ���Ӧ1/2/3
	   int      sensitive = 0;
	   int      severity  = 0;
	   int[][]  severityArr = {
			    {1,1,1,1,1,1,1},//������Ϊ��
	            {50,10,10,1,1,20,20},//������Ϊ��
	            {500,100,100,10,10,500,500},//������Ϊ��
	            {5000,1000,1000,1,1,5000,5000}//������Ϊ��
	   };
	    switch(SCCMechId){
//	    ��֪���س̶�ָ��
	    case 0:
	        severity = severityPara;
	    	break;
//	   ��������Ըߵ�
	    case 1:
	    	/*�漰�Ĳ���
	    	 * 1��NaOHŨ��
	    	 * 2���Ƿ����Ӧ������
	    	 * 3�����������¶�
	    	 * 4���Ƿ�������ɨ
	    	 * 5���Ƿ����
	    	 * */
	    	if(isStressRelief == 1){
	    		sensitive=0;
	    	}else{
	    		 if(78-0.6*NaOHConcentration < operatingTemp){
//                   NaOHŨ���Ƿ�С��5%
                    if(NaOHConcentration<0.05){
//                       �Ƿ����
                        if(isHeatTreacing == 1){
                            sensitive=2;
                        }else{
//                           �Ƿ�������ɨ
                            if(isSteamBlowing == 1){
                                sensitive = 1;
                            }else{
                                sensitive = 0;
                            }
                        }
                    }else{
//                       �Ƿ����
                        if(isHeatTreacing == 1){
                            sensitive = 3;
                        }else{
//                           �Ƿ�������ɨ
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
/*������Ӧ����ʴ����������ɸѡ
�漰�Ĳ���
1����������
2����Һ�ĳɷ�  1�������ʰ�δ����H2S��CO2  2����ƶ������Ũ��H2S��CO2  3���������и�Ũ��H2S��CO2
3����߹����¶�
4���Ƿ����
5��������ɨ
6���Ƿ�����Ӧ��
*/
//	    	�Ƿ�Ӧ������
	    if(isStressRelief == 1){
//              ��������Ϊ0.����Ϊ�ߡ��С��ͣ���Ӧ1/2/3
              sensitive=0;
          }else{
//              ����������ƶ����MEA��DIPA��DEA
              if(surrounding.equals("ƶ��")){
                  if(surrounding.equals("MEA") || surrounding.equals("DIPA")){
                      if(operatingTemp > 82){
                          sensitive = 3;
                      }else{
                          if( operatingTemp > 32 && operatingTemp < 82){
                              sensitive = 2;
                          }else{
//                                �Ƿ����
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
//	    	����Ӧ����ʴ����������ɸѡ
	    	/*�漰�Ĳ�����
	    	 * 1���Ƿ����ˮ
	    	 * 2��ˮ�е����⺬��
	    	 * 3��ˮ�е�pHֵ
	    	 * 4���Ƿ�����軯��
	    	 * 5�������Ӳ��
	    	 * 6���Ƿ񺸺��ȴ���
	    	 * */
	    	if(isMediumWater == 1){
//              ˮ�е�H2S��������������
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
//              ˮ�е�PHֵ����������
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
             
              //�������̶ȵı�׼��� ��22610.4 ��D.9
              int[][] environmentalSeverityArr={
//                  1 �����  2 ������  3 �����
                  {1,2,3,3},
                  {1,1,1,2},
                  {1,2,2,2},
                  {1,2,2,3},
                  {1,2,3,3}
              };
              int environmentalSeverity=environmentalSeverityArr[SCCWaterpHIndex][waterH2SConcentrationIndex];
              environmentalSeverity= environmentalSeverity*1-1;
//              sscc�����Ա�׼���� ��22610.4 ��D.10
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
//              ���ж�1��ʾ�ͣ�2��ʾ�У�3��ʾ��
               sensitive = sensitiveArr[environmentalSeverity][SCCBHardnessIndex];
          }else{
              sensitive = 0;
          }
	      int sensitiveSSCC=sensitive;
//-----------------------------------------ʪ���⻷�������¿��Ѻ�Ӧ���������¿���(HIC/SOHIC-H2S)---------------------------------------------
//�漰������
//1���Ƿ����ˮ
//2��ˮ���Ƿ����H2S
//3��ˮ��pHֵ
//4���Ƿ����軯��
//5���ְ��е�����
//6���ֲ�Ʒ��ʽ   1�����Ƹְ庸�� /2���޷�ֹ�����

           if(isMediumWater == 1){
//              ˮ�е�H2S��������������
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
//               ˮ�е�PHֵ����������
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
//              �������̶ȵı�׼��� ����22610.4 ��D.12
               int[][] environmentalSeverityArr = {
//                   1 �����  2 ������  3 �����
                   {1,2,3,3},
                   {1,1,1,2},
                   {1,2,2,2},
                   {1,2,2,3},
                   {1,2,3,3}
               };
//              ����������̶�
               int environmentalSeverity = environmentalSeverityArr[SCCWaterpHIndex][waterH2SConcentrationIndex];
//              �����껷�����̶�����
                environmentalSeverity = environmentalSeverity*1-1;
//              �������������Ƿ񺸺��ȴ�������
                int SCCSteelSulfurIndex=1;
               if(isHeatTreatAfterWeld == 1){
//                  �ְ��е����� (�Ѿ������ȴ����)
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
//               SSCC�����Ա�׼���� ��22610.4 ��D.13
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
           //��������Ľ�������س̶�ָ��
           severity = severityArr[sensitive][3];
	    	break;
	    case 4:
//------------------------------------̼���θ�ʴ����---------------------------------------
/*�漰�Ĳ���
 * 1���Ƿ�Ӧ������
 * 2��̼����Ũ��
 * 3��ˮ��pHֵ
 */          
	    	if(isStressRelief == 1){
                sensitive = 0;
            }else if(isStressRelief == 0) {
                if (isMediumWater == 1) {
                    if (SCCWaterH2S > 0.00005) {
                        //̼����Ũ��
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
                        //ˮ��PHֵ
                    	int SCCWaterpHIndex = 0;
                    	if(SCCWaterpH >= 7.8 && SCCWaterpH < 8.3){
                    		SCCWaterpHIndex = 0;
                    	}else if(SCCWaterpH >= 8.3 && SCCWaterpH < 8.9){
                    		SCCWaterpHIndex = 1;
                    	}else if(SCCWaterpH < 8.9){
                    		SCCWaterpHIndex = 2;
                    	}
//                        ̼���������Ա�׼���22610.4 ��D.15
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
// -----------------�������Ὺ�ѣ�PTA��-----------------------------
//      �漰������
//      1���������
//      2������ʷ  1��������˻� 2����ǰ�ȶ�����  3�������ȴ���
//      3����������¶�
//      4���Ƿ����ͣ������
//      ��������¶Ȳ�������¶�
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
// --------�Ȼ���Ӧ����ʴ���ѣ�CISCC��----------------------------------------------
//�漰������
//1������ˮ��CL-Ũ��
//2�������¶�
//3������ˮ��pHֵ
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
	    //--------------------------------ȷ��SCCӦ����ʴ��������-------------------------------------
	 // Ӧ����ʴ�������Ӹ��ݱ�׼22610.4 ��D.5
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
	  String   checkCondition           = "0";  //��������ͼ�����Ч������ֵ���硰1A������4D��
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
//	    �����������ͼ�����Ч������ֵ
	  int checkConditionIndex = 0;
		for (int i = 0; i < checkConditionIndexArr.length; i++) {  
          if (checkConditionIndexArr[i].equals(checkCondition)) {  
          	 checkConditionIndex  =  i;
          }  
      }
//	    ���������Գ̶�����ֵ
	  int severityIndex = 0;
		for (int i = 0; i < SCCTableYIndexArr.length; i++) {  
          if (SCCTableYIndexArr[i] == severity) {  
        	  severityIndex  =  i;
          }  
      }
	  
	  double SCCFactor = SCCArr[severityIndex][checkConditionIndex];
	  return SCCFactor;
   }	
  
//--------------------------------------�ⲿ�������Ӽ���ģ��---------------------------------------------------------
  public double getOutDamageFactor(
		double  operatingTemp,               //�����¶�
		double  designTemp,                  //����¶�
		int     part,                        //���޲�λ��1����ڰ壬0����װ�
		double  thickness,                   //�װ��ڰ�ԭʼ���
		
		int     pipeComplexity,               //�ܵ����Ӷ�
		int     isKeepWarmHasCL,              //�Ƿ��²㺬��
		int     isPipeSupport,                //�Ƿ��֧�ܲ���
		int     isInterfaceCompensation,      //�Ƿ���油��
		
		
		int     outDamageMechanismId,        //�ⲿ���˻���
		double  useDate,                     //ʹ��ʱ��
		int     isKeepWarm,                  //�Ƿ��б��²�
		int     KeepWarmStatus,              //���²�����
		int     coatingStatus,               //Ϳ������
		
		double  coatingUseDate,              //Ϳ��ʹ��ʹ��ʱ��
		String  linkType,                    //������ʽ
		int     SCCMechanismId,              //SCC���˻���
		int     checkTime,                   //�������
		String  checkValidity,               //������Ч��
		
		int     geographyEnvironment,        //�豸����
		int     isMaintenanceAsRequired,     //�Ƿ���Ҫ�����ά��
		int     tankFoundationSettlement,     //������������
		int    isMediumWater,                  //�Ƿ���ʺ�ˮ
		double SCCWaterH2S,                    //ˮ�е����⺬��
		double SCCWaterpH,                     //ˮ��pHֵ
		int    isHeatTreatAfterWeld,            //�Ƿ񺸺��ȴ���
		double SCCBHardness,                   //�����Ӳ��
		double clConcentration,                //ˮ�е������Ӻ���
		
		int    isHeatTreacing,                 //�Ƿ����
		double NaOHConcentration,               //NaOHŨ��
		int    isSteamBlowing,                 //�Ƿ�������ɨ
		int    isStressRelief,                  //�Ƿ����Ӧ������
		int    SCCHeatHistory,                  //����ʷ
		int    SCCisShutdownProtect,            //�Ƿ�ͣ������
		String surrounding,                     //����������
		double SCCSteelSulfur,                  //�ְ�������
		double SCCWaterCarbonateConcentration   //̼����Ũ��

  ){
	double outDamageFactor = 1;                        //��ʼ���ⲿ��������
	Calculate calculate = new Calculate(); 
	
	switch(outDamageMechanismId){
	//------------------------------------------------̼�ֺ͵ͺϽ�ֵ��ⲿ��ʴ�����ⲿ��������-----------------------------------------------------
		case 1: //1����"̼�ֺ͵ͺϽ�ֵ��ⲿ����"
	//	     �����������������¶ȣ�
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
   
//	               ��������������������أ�
	int outCarbonSteelCorrosionSpeedTableXIndex=0;
    switch (geographyEnvironment){
        case 1: //1����'�ȴ�/����'
            outCarbonSteelCorrosionSpeedTableXIndex=0;
            break;
        case 2://2����'�´�/�º�'
            outCarbonSteelCorrosionSpeedTableXIndex=1;
            break;
        case 3://3����'�ɺ�/ɳĮ'
            outCarbonSteelCorrosionSpeedTableXIndex=2;
            break;
        default:
            outCarbonSteelCorrosionSpeedTableXIndex=1;
            break;
    }

//	                ̼�ֺ͵ͺϽ�ֵ��ⲿ��ʴ���ʱ�񣬰���22610.4 ��I.3
    double[][] outCarbonSteelCorrosionSpeedArr={
        {0,0,0},
        {0.13,0.076,0.025},
        {0.05,0.025,0},
        {0.13,0.05,0.025},
        {0.025,0,0},
        {0,0,0}
    };
    double outCarbonSteelCorrosionSpeed = outCarbonSteelCorrosionSpeedArr[outCarbonSteelCorrosionSpeedTableYIndex][outCarbonSteelCorrosionSpeedTableXIndex];
//	                ����Ϳ�������ĵ���
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
//	                ���ݹ�֧�ܲ����ĵ���

    if(isPipeSupport==1){
        outCarbonSteelCorrosionSpeed=outCarbonSteelCorrosionSpeed*2;
    }
//	                ���ݽ��油������
    if(isInterfaceCompensation==1){
        outCarbonSteelCorrosionSpeed=outCarbonSteelCorrosionSpeed*2;
    }
//	                �ⲿ�������ӵ�ʵ���Ǽ�����������
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
//-------------------̼�ֺ͵ͺϽ�ֵ�CUI��ʴ-------------------------------------------------------
//2����"̼�ֺ͵ͺϽ�ֵ�CUI��ʴ"
	case 2: 
//�����������������¶ȣ�
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
	//	               ��������������������أ�
	int outCarbonSteelCUICorrosionSpeedTableXIndex=0;
	switch (geographyEnvironment){
	    case 1:  //1����'�ȴ�/����'
	        outCarbonSteelCUICorrosionSpeedTableXIndex=0;
	        break;
	    case 2: //2����'�´�/�º�'
	        outCarbonSteelCUICorrosionSpeedTableXIndex=1;
	        break;
	    case 3: //3����'�ɺ�/ɳĮ'
	        outCarbonSteelCUICorrosionSpeedTableXIndex=2;
	        break;
	}
	
	//	                ̼�ֺ͵ͺϽ�ֵ��ⲿ��ʴ���ʱ�񣬰���22610.4 ��I.3
	    double[][] outCarbonSteelCUICorrosionSpeedArr = {
	        {0,0,0},//С��-12
	        {0.05,0.03,0.01},
	        {0.02,0.01,0},
	        {0.10,0.05,0.02},
	        {0.02,0.01,0},
	        {0,0,0}
	    };
    double outCarbonSteelCUICorrosionSpeed = outCarbonSteelCUICorrosionSpeedArr[outCarbonSteelCUICorrosionSpeedTableYIndex][outCarbonSteelCUICorrosionSpeedTableXIndex];
//	                ����Ϳ�������ĵ���
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
//	                ���ݹܵ����Ӷȵ���
    switch(pipeComplexity){
        case 1://����ƽ��ˮƽ
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*0.75;
            break;
        case 2://ƽ��ˮƽ
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.0;
            break;
        case 3://����ƽ��ˮƽ
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.25;
            break;
        default:
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.0;
            break;
    }
//	                ���ݱ��²�״������
    switch(KeepWarmStatus){
        case 1://����ƽ��ˮƽ
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*0.75;
            break;
        case 2://ƽ��ˮƽ
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.0;
            break;
        case 3://����ƽ��ˮƽ
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.25;
            break;
        default:
            outCarbonSteelCUICorrosionSpeed=outCarbonSteelCUICorrosionSpeed*1.0;
            break;
    }
//	                ���ݹ�֧�ܲ����ĵ���
    
    if(isPipeSupport == 1){
        outCarbonSteelCUICorrosionSpeed = outCarbonSteelCUICorrosionSpeed*2;
    }
//	                ���ݽ��油������
    if(isInterfaceCompensation == 1){
        outCarbonSteelCUICorrosionSpeed = outCarbonSteelCUICorrosionSpeed*2;
    }
//	          �ⲿ�������ӵ�ʵ���Ǽ�����������
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

// -------------------------------------------�����岻��ֵ��ⲿSCC--------------------------------------------
case 3://3����"�����岻��ֵ��ⲿSCC"
//	   �����������������¶ȣ�
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
	//	               ��������������������أ�
	int outAusteniticCorrosionSpeedTableXIndex=1;
	switch (geographyEnvironment){
	    case 1: //1����'�ȴ�/����'
	        outAusteniticCorrosionSpeedTableXIndex=0;
	        break;
	    case 2://2����'�´�/�º�'
	        outAusteniticCorrosionSpeedTableXIndex=1;
	        break;
	    case 3://3����'�ɺ�/ɳĮ'
	        outAusteniticCorrosionSpeedTableXIndex=2;
	        break;
	}

//	                �����岻��ֵ��ⲿSCC��񣬰���22610.4 ��I.17  0������ 1 ����� 2 ������  3 �����
    int[][] outAusteniticSensitiveArr = {
        {0,0,0},//С��60
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
//	                ����Ϳ����������Ϳ��Ϳװ����
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
//4����"�����岻���CUI�ⲿSCC"
//�����������������¶ȣ�
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
//	               ��������������������أ�
	int outAusteniticCUISensitiveTableXIndex = 1;
    switch (geographyEnvironment){
        case 1: //1����'�ȴ�/����'
            outAusteniticCUISensitiveTableXIndex = 0;
            break;
        case 2://2����'�´�/�º�'
            outAusteniticCUISensitiveTableXIndex = 1;
            break;
        case 3://3����'�ɺ�/ɳĮ'
            outAusteniticCUISensitiveTableXIndex = 2;
            break;
    }
//	                �����岻��ֵ�CUI�ⲿSCC��񣬰���22610.4 ��I.17  0������ 1 ����� 2 ������  3 �����
    int[][] outAusteniticCUISensitiveArr = {
        {0,0,0},//С��60
        {3,2,1},
        {2,1,0},
        {0,0,0}
    };
    outAusteniticSensitive = outAusteniticCUISensitiveArr[outAusteniticCUISensitiveTableYIndex][outAusteniticCUISensitiveTableXIndex];
//	               ����Ϳ����������Ϳװʱ��
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
//	                ���ݹܵ����Ӷȵ���
    switch(pipeComplexity){
        case 1://����ƽ��ˮƽ
            outAusteniticSensitive = outAusteniticSensitive-1;
            break;
        case 2://ƽ��ˮƽ
            outAusteniticSensitive = outAusteniticSensitive+0;
            break;
        case 3://����ƽ��ˮƽ
            outAusteniticSensitive = outAusteniticSensitive+1;
            break;
    }
//	                ���ݱ��²�״������
    if(isKeepWarm == 1){
        switch(KeepWarmStatus){
            case 1://����ƽ��ˮƽ
                outAusteniticSensitive = outAusteniticSensitive-1;
                break;
            case 2://ƽ��ˮƽ
                outAusteniticSensitive = outAusteniticSensitive+0;
                break;
            case 3://����ƽ��ˮƽ
                outAusteniticSensitive = outAusteniticSensitive+1;
                break;
            default:
                outAusteniticSensitive = outAusteniticSensitive+0;
                break;
        }
    }
    //                ���ݱ��²㺬���������

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
//-----------------------------------------------���Զ�����������---------------------------------------------------
//���Զ�����������û�У�Ĭ��Ϊ0
 
  
//--------------------------------------����ʧЧ���----------------------------------------------------------------
  ///  �ڰ��ʧЧ������㣬ÿһ�㵥������  �ο�C.13.2
  /// </summary> //
  /// <param name="fTankDiameter_">��ֱ������λ��m</param>
  /// <param name="fCHT_">���޵���ڰ�߶ȣ���λ��m</param>
  /// <param name="iFloor_">�ڰ�ڼ���</param>
  /// <param name="fAcceptBaseQ_">ΪʧЧ����ɽ���ˮƽ�Ļ�׼,��λ����Ԫ</param>
  /// <param name="iEnvSensitibility_">�������ж�.  0-�͡�1-�С�2-��</param>
//  / <param name="eLeakHoleSize_">й¶�׳ߴ��С.ö�����ͱ���</param>
  /// <param name="fHliq_">й¶���Ϸ���Һ��߶ȣ����ܾ��Ǵ����ڵ�ʵ��Һ��߶ȣ�����λ��m</param>
  /// <param name="fMatCost_">���ϼ۸�ϵ��������ΪQ235A����ȡ1.0���������ϵĲ��ϼ۸�ϵ��Ϊ��Q235A����ʵ�ʼ۸�ı�ֵ</param>
  /// <param name="fProd_">ͣ����ɵ���ʧ����λ����Ԫ</param>
  /// <param name="fPlvdike_">���Χ��������ٷֱ�</param>
  /// <param name="fPonsite_">���Χ�������ڹ����ڣ��ر������е�����ٷֱ�</param>
  /// <param name="fPoffsite_">���Χ���������������⣬�ر������е�����ٷֱ�</param>
  /// <param name="fBblL_leak1_">й������£�Сй¶�׶�Ӧ������й������m3</param>
  /// <param name="fBblL_leak2_">й������£���й¶�׶�Ӧ������й������m3</param>
  /// <param name="fBblL_leak3_">й������£���й¶�׶�Ӧ������й������m3</param>
  /// <returns>0-A, 1-B, 2-C,3-D,4-E</returns>
  public double getFailureWallConsequence(
		  double  fTankDiameter_,      //��ֱ��
	      double  fCHT_,               //���޵���ڰ�߶�
	      int     iFloor_,             //�ڼ���ڰ�
	      int     iEnvSensitibility_,  //ʧЧ����ɽ��ܵĻ�׼ ��λ����Ԫ
	      double  fHliq_,              //й¶���Ϸ���Һ��߶� 
	      double  fMatCost_,           //���ϼ۸�ϵ��
	      double  fProd_,              //ͣ����ɵ���ʧ
	      double  fPlvdike_,           //���Χ�ߵ�����ٷֱ�
	      double  fPonsite_,           //���Χ�������ڹ����ڣ��ر������е�����ٷֱ�
	      double  fPoffsite_           //���Χ���������������⣬�ر������е�����ٷֱ�
		  ){
  if(iEnvSensitibility_ >= 3){
	  iEnvSensitibility_ = 2;
  }
  //�ڰ岻ͬ�ߴ�й¶�׺����ѵ�ƽ��ʧЧ���� 1-С��2-�У�3-��4-����
  double fG1 = 0.00007; double fG2 = 0.000025; double fG3 = 0.000006; double fG4 = 0.0000001;
  //й¶��ʧЧ���� �ο�����8.3
  double fGTotal = fG1 + fG2 + fG3 + fG4;

  //ȷ��й¶��ֱ��(��λ��mm) �ο���C.3

  double fLeakHole1_small_Dia   = 3.0;
  double fLeakHole2_middle_Dia  = 6.0;
  double fLeakHole3_large_Dia   = 50.0;
  double fLeakHole4_Rupture_Dia = 1000*fTankDiameter_/4.0;

  //��ͬй¶�׶�Ӧ��й�����ʼ��� �ο�C.4.2
  double fW1_small    =  0.086 * 0.61 * Math.PI * Math.pow(fLeakHole1_small_Dia, 2) / 4 * Math.sqrt(2 * 9.81 * fHliq_);
  double fW2_middle   =  0.086 * 0.61 * Math.PI * Math.pow(fLeakHole2_middle_Dia, 2) / 4 * Math.sqrt(2 * 9.81 * fHliq_);
  double fW3_large    =  0.086 * 0.61 * Math.PI * Math.pow(fLeakHole3_large_Dia, 2) / 4 * Math.sqrt(2 * 9.81 * fHliq_);
  double fW4_rupture  =  0.086 * 0.61 * Math.PI * Math.pow(fLeakHole4_Rupture_Dia, 2) / 4 * Math.sqrt(2 * 9.81 * fHliq_);

  //ȷ����i��ڰ��ϵ�Һ��߶� �ο�����4.1
  double fLHTabove = fHliq_ - (iFloor_ - 1) * fCHT_;
  //ȷ����i��ڰ��ϵ�Һ����� �ο�����4.2
  double fLvol_above = Math.PI * Math.pow(fTankDiameter_, 2) / 4 * fLHTabove;

  //ȷ����ͬй¶�׳ߴ��������Чй���� �ο�����4.3��4.4
  double fBbl_avail1_small   = fLvol_above;
  double fBbl_avail2_middle  = fLvol_above;
  double fBbl_avail3_large   = fLvol_above;
  double fBbl_avail4_rupture = fLvol_above;

  //й�ż��ʱ�� �ο�����6.2
  // double ftld = 7;
  //ȷ����ͬй¶�׳ߴ��й�ų���ʱ��
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

  //ȷ����ͬй¶�׳ߴ������й����  �ο�����6.4
  double fBblL_leak1 = Math.min(fW1_small * fld1_small, fBbl_avail1_small);
  double fBblL_leak2 = Math.min(fW2_middle * fld2_middle, fBbl_avail2_middle);
  double fBblL_leak3 = Math.min(fW3_large * fld3_large, fBbl_avail3_large);

  //ȷ����������µĶ�Ӧ������й����
  double fBbl_rupture = fld4_rupture;


  double fBbl_Total = fHliq_ * Math.pow(fTankDiameter_, 2) * Math.PI / 4;

  ///������ʧ���ú��

  ///��ͬ�������ж��µĸ���������ú������λ����Ԫ��. �ֱ�Ϊ�ͣ�0�����У�1�����ߣ�2��������������飬��ʾ��ͬ
  //����й�ŵ�Χ����ʱ�Ļ������ú��
  double fEnvCindike              = 0.04;
  //����й�ŵ���������������ʱ�Ļ������ú��
  double fEnvCss_onsite           = 0.2;
  //����й�ŵ���������������ʱ�Ļ������ú��
  double[] faEnvCss_offsite       = {0.4,1,2};
  //����й�ŵ�����������ʱ�Ļ��ƾ��ú��
  //double[] faEnvCsubsoil          = {2, 6, 12};
  //����й�ŵ�����ˮ��ʱ�Ļ������ú��
  //double[] faEnvCgroundwater      = {4, 20,40};
  //����й�ŵ��ر�ˮ��ʱ�Ļ������ú��
  double[] faEnvCwater            = {2, 6, 20};

  ///й¶���
  //�ڰ�й¶��������й������� �ο�����8.4
  double fBblL_release = (fBblL_leak1 * fG1 + fBblL_leak2 * fG2 + fBblL_leak3 * fG3 + fBbl_rupture * fG4) / fGTotal;

  //й©��й�ź�����Χ���ڵ���������� �ο�����8.5
  double fBblL_indike = fBblL_release * (1 - fPlvdike_);
  //й�ŵ������������������������� �ο�����8.5
  double fBblL_ss_onsite = fPonsite_ * (fBblL_release - fBblL_indike);
  //й�ŵ������������������������� �ο�����8.5
  double fBblL_ss_offsite = fPoffsite_ * (fBblL_release - fBblL_indike - fBblL_ss_onsite);
  //�ѵ���ˮԴ�����������  �ο�����8.5
  double fBblL_water = fBblL_release - (fBblL_indike + fBblL_ss_onsite + fBblL_ss_offsite);
  //й¶���µĻ�����ʧ �ο�����8.6
  double fFCenv_leak = fBblL_indike * fEnvCindike + fBblL_ss_onsite * fEnvCss_onsite + fBblL_ss_offsite * faEnvCss_offsite[iEnvSensitibility_] + fBblL_water * faEnvCwater[iEnvSensitibility_];

  ///������ص����
  //��������µ�����й�������
  double fBblR_release = fBbl_Total * fG4/fGTotal;
  //�������������Χ���ڵ����������
  double fBblR_indike = fBblR_release*(1-fPlvdike_);
  //����������ڹ�����������������������
  double fBblR_ss_onsite = fPonsite_ *(fBblR_release - fBblR_indike);
  //����������ڹ�����������������������
  double fBblR_ss_offsite = fPoffsite_ * (fBblR_release - fBblR_indike - fBblR_ss_onsite);
  //����������ѵ���ˮԴ�����������
  double fBblR_water = fBblR_release - (fBblR_indike + fBblR_ss_onsite + fBblR_ss_offsite);

  //���ѵ��µĻ�����ʧ���ú��
  double fFCenv_rupture = fBblR_indike * fEnvCindike + fBblR_ss_onsite * fEnvCss_onsite + fBblR_ss_offsite * faEnvCss_offsite[iEnvSensitibility_] + fBblR_water * faEnvCwater[iEnvSensitibility_];

  double fFC_Eviron = fFCenv_leak + fFCenv_rupture;

  ///й¶���µ��豸�𻵾��ú��
  double fFC_Emd = fMatCost_ * (fG1 * 4 + fG2 * 9.6 + fG3 * 16 + fG4 * 32) / fGTotal;

  ///й¶��ͣ����ʧ���ú��
  double fFC_Prod = fProd_ * (fG1 * 2 + fG2 * 3 + fG3 * 3 + fG4 * 7) / fGTotal;

  double fFCTotal = fFC_Prod + fFC_Emd + fFC_Eviron;

  return fFCTotal;
  }
  
// <summary>
  // �װ�ʧЧ������㡣�ο���׼GBT-30578�и�¼C
  // </summary> ����λ��m</param>
  // <param name="fHliq_">���ڵ�Һ��߶ȣ���λ��m</param>
  // <param name="fAcceptBaseQ_">ΪʧЧ����ɽ���ˮƽ�Ļ�׼,��λ����Ԫ</param>
  // <param name="iEnvSensitibility_">�������ж�.  0-�͡�1-�С�2-��</param>
  // <param name="eLeakHoleSize_">й¶�׳ߴ��С.ö�����ͱ���</param>
  // <param name="fMatCost_">���ϼ۸�ϵ��������ΪQ235A����ȡ1.0���������ϵĲ��ϼ۸�ϵ��Ϊ��Q235A����ʵ�ʼ۸�ı�ֵ</param>
  // <param name="fProd_">ͣ����ɵ���ʧ����λ����Ԫ</param>
  // <param name="fPlvdike_">���Χ��������ٷֱ�</param>
  // <param name="fPonsite_">���Χ�������ڹ����ڣ��ر������е�����ٷֱ�</param>
  // <param name="fPoffsite_">���Χ���������������⣬�ر������е�����ٷֱ�</param>
  // <param name="iLeakHoleSize_">й������£�Сй¶�׶�Ӧ������й������m3</param>
  // <param name="fSgw_">�޵׵�����ˮ�ľ��룬��λ��m</param>
  // <param name="fMedium_p_">�����ܶ�,��λ��kg/m3;</param> �������ܶȺͶ���ճ�ȿ������û�ɸѡ(��C.2)������ֱ����д
  // <param name="fMedium_DynVisc_">���ʶ���ճ��,��λ��N*s/m2 �� pa*s;</param> ����ճ�ȣ�Dynamic viscosity
  // <param name="iTankBaseType_">���޻�����ʽ��0---����Ϊˮ�������  1������������RPB��2��������û��RPB</param>
  // <param name="eTankSubsoilType_">���޻���������������</param>
  // <returns>0-A, 1-B, 2-C,3-D,4-E</returns>
  public double getFailurefloorConsequence(
      double fTankDiameter_,     //��ֱ��
      double fHliq_ ,            //���ڵ�Һ��߶�
      int    iEnvSensitibility_, //ʧЧ����ɽ���ˮƽ�Ļ�׼
      double fMatCost_,          //���ϼ۸�ϵ��
      double fProd_,             //ͣ����ɵ���ʧ
      double fPlvdike_,          //���Χ��������ٷֱ�
      double fPonsite_,          //���Χ�������ڹ����ڣ��ر������е�����ٷֱ�
      double fPoffsite_,         //���Χ���������������⣬�ر������е�����ٷֱ�
      double fSgw_,              //�޵׵�����ˮ�ľ���
      double fMedium_p_,         //�����ܶ�
      double fMedium_DynVisc_,   //���ʶ���ճ��ϵ��
      int    iTankBaseType_,     //���޻�����ʽ
      int    eTankSubsoilType_   //���޻��������������� 1�����ɰ 2����ϸɰ 3��ϸɰ 4�����ɰ 5����ɰ��� 6������� 7���������-����
  ){

	  if(iEnvSensitibility_ >= 2){
		  iEnvSensitibility_=2;
	  }
	  
      //�װ岻ͬ�ߴ�й¶�׺����ѵ�ƽ��ʧЧ���� 1-С��2-�У�3-��4-����
      double fG1 = 0.00007; double fG2 = 0.000025; double fG3 = 0.000006; double fG4 = 0.0000001;
      //й¶��ʧЧ����
      double fGTotal = fG1 + fG2 + fG3 + fG4;

      /// ������϶�ʣ� ˮѹ�����ʵ�����ֵ��ˮѹ�����ʵ�����ֵ
//      1�����ɰ 2����ϸɰ 3��ϸɰ 4�����ɰ 5����ɰ��� 6������� 7���������-����
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
      double fpw =1000;//ˮ���ܶ�:kg/m3
      double fuw = 0.001;//ˮ�Ķ���ճ�� N*s/m2 �� pa*s
      double fKh = fKh_water*(fMedium_p_/fpw)*(fuw/fMedium_DynVisc_);//������ˮѹ������
      double fVels_prod = fKh/fPs;///������͸����

      //С���С�������й¶�׳ߴ��Ӧ��й¶��ֱ��.��λ��mm
      //ȷ��й¶�׳ߴ磬�ο���C.4
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
      //��ͬй¶�׳ߴ��Ӧй¶������ �ο���C.5
      double iLeakHole_middle_num = 0;
      double iLeakHole_large_num  = 0;
      double iLeakHole_small_num  = Math.max((Math.round((Math.pow((fTankDiameter_/30),2)))),1);

      //й�ż��ʱ��
      //C.8.2 �ο�����7.2 0����ˮ������� 1��������RPB 2����û����RPB
      double ftld = 30.0;
      if(iTankBaseType_ == 2){
          ftld = 30.0;
      }else if(iTankBaseType_ == 1){
          ftld = 360.0;
      }else if(iTankBaseType_ == 0){
          ftld = 7.0;
      }

      ///������ʧ���ú��
      ///��ͬ�������ж��µĸ���������ú������λ����Ԫ��. �ֱ�Ϊ�ͣ�0�����У�1�����ߣ�2��������������飬��ʾ��ͬ
      //����й�ŵ�Χ����ʱ�Ļ������ú��
      double fEnvCindike = 0.04;
      //����й�ŵ���������������ʱ�Ļ������ú��
      double fEnvCss_onsite = 0.2;
      //����й�ŵ���������������ʱ�Ļ������ú��
      double[] faEnvCss_offsite = {0.4, 1, 2};
      //����й�ŵ�����������ʱ�Ļ��ƾ��ú��
      double[] faEnvCsubsoil = {2, 6, 12};
      //����й�ŵ�����ˮ��ʱ�Ļ������ú��
      double[] faEnvCgroundwater = {4, 20, 40};
      //����й�ŵ��ر�ˮ��ʱ�Ļ������ú��
      double[] faEnvCwater = {2, 6, 20};

      ///й¶���
      //����й�ŵ�����ˮ��ʱ��
      double fTgl = fSgw_ / fVels_prod;

//      //��й������£��ֱ��Ӧ����С���С���й¶�׳ߴ�ĵ���ˮ����������
//      $fBblL_gw1 = .0; $fBblL_gw2 = .0; $fBblL_gw3 = .0;
//
//      //��ͬй¶�׳ߴ��£����޵װ��й������
//      $fW1_small = .0; $fW2_middle = .0; $fW3_large = .0;


      //���������ĽӴ��̶ȵ���ϵ��
      //�ο�C4.3.2
      double fCqo ;
      double fhliq ;
      if(iTankBaseType_ == 1)
      {
          //������з�й©���������������ȡֵ
          fCqo = 0.21;
          fhliq = 0.08;
      }else{
          fCqo = 1.15;//���������Ӵ��̶ȵĵ���ϵ��
          fhliq = fHliq_;//�����ڵ�Һ��߶�
      }

      //���޵װ��й�����ʼ���  �ο�����3.4
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

      //�������������й���� C5.3 ����5.1��5.2
      double fBbl_Total = fhliq * (Math.PI * Math.pow(fTankDiameter_, 2.0)) / 4.0;

      //ȷ��й�ų���ʱ�� �ο�����7.3
      double fld1_small  = Math.min((fBbl_Total/fW1_small),ftld);
      double fld2_middle = Math.min((fBbl_Total/fW2_middle),ftld);
      double fld3_large  = Math.min((fBbl_Total/fW3_large),ftld);

      //ȷ����ͬй¶�׳ߴ��µĶ�Ӧ������й���� �ο�����7.4
      double fBblL_leak1 = Math.min((fW1_small * fld1_small),fBbl_Total);
      double fBblL_leak2 = Math.min((fW2_middle * fld2_middle),fBbl_Total);
      double fBblL_leak3 = Math.min((fW3_large * fld3_large),fBbl_Total);
      //double fBblL_leak4 = fBbl_Total;

      //ÿ��й¶�׳ߴ�й�ŵ��޵׵�������� �ο���¼C�еĲ���9.5
      
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

      //й¶���µĻ�����ʧ������9.6
      double fFCenv_leak = ((fBblL_gw1 * faEnvCgroundwater[iEnvSensitibility_] + fBblL_subsoil1 * faEnvCsubsoil[iEnvSensitibility_]) + (fBblL_gw2 * faEnvCgroundwater[iEnvSensitibility_] + fBblL_subsoil2 * faEnvCsubsoil[iEnvSensitibility_]) + (fBblL_gw3 * faEnvCgroundwater[iEnvSensitibility_] + fBblL_subsoil3 * faEnvCsubsoil[iEnvSensitibility_])) / fGTotal;

      ///������ص����
      //��������µ�����й�������
      double fBblR_release = fBbl_Total * fG4 / fGTotal;
      //�������������Χ���ڵ����������
      double fBblR_indike  = fBblR_release * (1 - fPlvdike_);
      //����������ڹ�����������������������
      double fBblR_ss_onsite = fPonsite_ * (fBblR_release - fBblR_indike);
      //����������ڹ�����������������������
      double fBblR_ss_offsite = fPoffsite_ * (fBblR_release - fBblR_indike - fBblR_ss_onsite);
      //����������ѵ���ˮԴ�����������
      double fBblR_water = fBblR_release - (fBblR_indike + fBblR_ss_onsite + fBblR_ss_offsite);
      //���ѵ��µĻ�����ʧ
      double fFCenv_rupture = fBblR_indike * fEnvCindike + fBblR_ss_onsite * fEnvCss_onsite + fBblR_ss_offsite * faEnvCss_offsite[iEnvSensitibility_] + fBblR_water * faEnvCwater[iEnvSensitibility_];

      //�ο�����9.10
      double fFC_Eviron = fFCenv_leak + fFCenv_rupture;
      if(fFC_Eviron < 0){
    	  fFC_Eviron = 0; 
      }
      ///й¶���µ��豸�𻵾��ú��     ����9.11
      double fFC_Emd = fMatCost_ * (fG1 * 4 +  fG4 * 96 * Math.pow((fTankDiameter_/30.0),2)) / fGTotal;

      ///й¶��ͣ����ʧ���  ����9.12
      double fFC_Prod = fProd_ * (fG1 * 5 +  fG4 * 50) / fGTotal;
 
      //����9.13
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
	      rightNow.add(Calendar.YEAR,year);//���ڼ�1��
	     
	      Date dt1=rightNow.getTime();
	       reStr = sdf.format(dt1); 
      } catch (ParseException e) {  
          e.printStackTrace();
      }   
      return reStr;
  }
  
}
