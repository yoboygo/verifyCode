/**
 * ImageMain.java
 * 
 * Aimy
 * 下午2:07:21
 */
package verifyCode;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**验证码识别
 * @author Aimy
 * 2015年1月5日 下午2:07:21
 */
public class ImageMain {
	
	//二值化的域阀值
	private static int POSITON = 150;
	
	private static String POSITON_X = "X";
	private static String POSITON_Y = "Y";
	
	/**
	 *  程序入口
	 * @author Aimy
	 * 2015年1月5日 下午5:24:55
	 */
	public static void main(String[] args)
	{
		String uri = "./image/1419926134540.png";
//		String uri = "./image/1419923496734.png";
		
		//读取图片
		BufferedImage bdImage = readImage(uri);
		//二值化
		transformToBin(bdImage);
		//获取切割点
		List<Map<String,Integer>> listPointCut = getCutPoint(bdImage);
		//切割图片
		List<BufferedImage> listSubImage = cutImage(bdImage,listPointCut);
		//打印到控制台
		consolePrint(listSubImage);
		
		//输出二值化后的图片
		writeImage(bdImage, "bmp", "./imageBin/test.bmp");
		
	}

	/**
	 *  图片先灰度化后二值化
	 * @author Aimy
	 * @param bdImage
	 * 2015年1月6日 上午9:09:11
	 */
	public static BufferedImage transformToBin(BufferedImage bdImage)
	{
		int iWidth = bdImage.getWidth();
		int iHeight = bdImage.getHeight();
		
		for(int y=0; y<iHeight; ++y)
		{
			for(int x=0; x<iWidth; ++x)
			{
				int rgb = bdImage.getRGB(x, y);
				
				int r=(rgb >> 16)&0xFF;
				int g=(rgb >> 8)&0xFF;
				int b=(rgb >> 0)&0xFF;
				//灰度化计算
				int grayPixel = (int)((b*29 + g*150 + r*77 + 128) >> 8);
				
				//二值化
				if(grayPixel >= POSITON)
				{
					bdImage.setRGB(x, y, Color.WHITE.getRGB());
				}else{
					bdImage.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		
		return bdImage;
	}
	
	/**
	 *  根据传入的URI读取图片文件
	 * @author Aimy
	 * @param uri
	 * @return
	 * 2015年1月5日 下午2:13:02
	 */
	public static BufferedImage readImage(String uri)
	{
		try {
			BufferedImage bi = ImageIO.read(new File(uri));
			return bi;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 *  将图片保存到磁盘
	 * @author Aimy
	 * @param bdImage
	 * @param fileName
	 * @param path
	 * 2015年1月6日 上午9:38:05
	 */
	public static void writeImage(BufferedImage bdImage,String formatName,String path)
	{
		try {
			ImageIO.write(bdImage, formatName, new FileOutputStream(new File(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  扫描图片，获取切割点的位置
	 * @author Aimy
	 * @param bdImage
	 * @return
	 * 2015年1月6日 上午10:16:45
	 */
	public static List<Map<String,Integer>> getCutPoint(BufferedImage bdImage)
	{
		int iWidth = bdImage.getWidth();
		int iHeight = bdImage.getHeight();
		List<Map<String,Integer>> pointCutList = new ArrayList<Map<String,Integer>>();
		
		//纵向切割
		int pointCount = 0;	//切割点计数
		for(int x=0; x<iWidth; ++x)
		{
			Map<String,Integer> pointCut = new HashMap<String, Integer>();
			for(int y=0; y<iHeight; ++y)
			{
				boolean isBlack = bdImage.getRGB(x, y) == Color.BLACK.getRGB();
				boolean isStart = pointCount%2 == 0;
				if(isBlack && isStart)//确定起点
				{
					++pointCount; //切割点计数
					pointCut.put(POSITON_X, x);
					pointCut.put(POSITON_Y, y);
					
					break;
				}else if(isBlack){//确定终点,可能存在黏连
					//判断是否是粘连的条件为：纵坐标上一个点和下一个点都是白点表示黏连
				//	/*
					if(y>0 && y<iHeight-1)
					{
						int frontPointRgb = bdImage.getRGB(x, y-1);
						int behindPointRgb = bdImage.getRGB(x, y+1);
						if(frontPointRgb==Color.WHITE.getRGB() && behindPointRgb==Color.WHITE.getRGB())
						{
							++pointCount; //切割点计数
							pointCut.put(POSITON_X, x);
							pointCut.put(POSITON_Y, y);
						}
						
					}
				//	*/
					break;
				}else if((y == iHeight-1) && !isStart)//确定终点,无黏连
				{
						++pointCount;
						pointCut.put(POSITON_X, x);
						pointCut.put(POSITON_Y, y);
						break;
				}
			}
			
			if(!pointCut.isEmpty())
				pointCutList.add(pointCut);
		}
		
		for(Map<String,Integer> pointCut:pointCutList)
		{
			System.out.println("x:"+pointCut.get(POSITON_X));
			System.out.println("y:"+pointCut.get(POSITON_Y));
		}
			
		return pointCutList;
	}
	
	/**
	 *  分割图片
	 * @author Aimy
	 * @param bdImage
	 * @param pointCut
	 * @return
	 * 2015年1月6日 上午11:30:15
	 */
	public static List<BufferedImage> cutImage(BufferedImage bdImage,List<Map<String,Integer>> pointCutList)
	{
		List<BufferedImage> listSubImage = new ArrayList<BufferedImage>();
		int iHeight = bdImage.getHeight();
		for(int i=0; i<pointCutList.size(); i+=2)
		{
			Map<String,Integer> pointCutStart = pointCutList.get(i);
			Map<String,Integer> pointCutEnd = pointCutList.get(i+1);
			int cutWidth = pointCutEnd.get(POSITON_X)-pointCutStart.get(POSITON_X);
			int cutHeight = iHeight-1;
			BufferedImage subImage = bdImage.getSubimage(pointCutStart.get(POSITON_X), 0, cutWidth, cutHeight);
			listSubImage.add(subImage);
			writeImage(subImage, "bmp","./imageBin/test_"+i+".bmp");
		}
		return listSubImage;
	}
	
	/**
	 *  打印到控制台
	 * @author Aimy
	 * @param listSubImage
	 * 2015年1月6日 下午5:18:27
	 */
	private static void consolePrint(List<BufferedImage> listImage) {
		for(BufferedImage bdImage:listImage)
		{
			int iWidth = bdImage.getWidth();
			int iHeight = bdImage.getHeight();
			
			for(int y=0; y<iHeight; ++y)
			{
				for(int x=0; x<iWidth; ++x)
				{
					if(bdImage.getRGB(x, y) == Color.BLACK.getRGB())
					{
						System.out.print(".");
					}else{
						System.out.print(" ");
					}
				}
				System.err.println("");
			}
			
		}
	}
}
