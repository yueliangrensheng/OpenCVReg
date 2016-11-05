package com.diwen.android.widget;

import java.util.ArrayList;
import java.util.List;

import com.diwen.android.bean.LineData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint({ "ClickableViewAccessibility", "DrawAllocation" })
public class CustomLineView extends View {
	
	private Paint paint;
	private Paint paintPoint;
	private Paint paintEditorPoint;
	private float startX;
	private float stopX;
	private float startY;
	private float stopY;
	private boolean isliga;  //开始填充
	private boolean isStart; //开始工作
	private List<LineData> lineDatas = new ArrayList<LineData>();
	private List<LineData> pintLineDatas = new ArrayList<LineData>();
	private Context context;
	public CustomLineView(Context context,float startX,float startY,float stopX,float stopY) {
		super(context);
		initPaint();
		this.startX = startX;
		this.startY = startY;
		this.stopX = stopX;
		this.stopY = stopY;
		this.context = context;
	}
	public CustomLineView(Context context) {
		super(context);
		this.context = context;
		initPaint();
	}
	public CustomLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initPaint();
	}
	
	private void initPaint(){
		isliga = false;
		paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStrokeJoin(Paint.Join.ROUND);    
        paint.setStrokeCap(Paint.Cap.ROUND);    
        paint.setStrokeWidth(3);  
        
        paintPoint = new Paint();
        paintPoint.setColor(Color.GRAY);
        paintPoint.setStrokeJoin(Paint.Join.ROUND);    
        paintPoint.setStrokeCap(Paint.Cap.ROUND);    
        paintPoint.setStrokeWidth(10);  
        
        paintEditorPoint = new Paint();
        paintEditorPoint.setColor(Color.RED);
        paintEditorPoint.setStrokeJoin(Paint.Join.ROUND);    
        paintEditorPoint.setStrokeCap(Paint.Cap.ROUND);    
        paintEditorPoint.setStrokeWidth(10);  
         
	}
	 //在这里我们将测试canvas提供的绘制图形方法    
    @Override    
    protected void onDraw(Canvas canvas) { 
    	int count = lineDatas.size();
    	for(int i =0;i<lineDatas.size();i++){  //绘制界面连线的点的
    		paintPoint.reset();//重置  
    		stopX = lineDatas.get(i).startX;
    		stopY = lineDatas.get(i).startY;
    		
    		canvas.drawCircle(stopX, stopY, 10, paintPoint); 
    		//canvas.drawPoint(stopX, stopY, paintPoint);//画一个点 
    		if(count == 1){
    			
    		}else{
    			if(i> 0){
					startX =  lineDatas.get(i-1).startX;
            		startY = lineDatas.get(i-1).startY;
        			paint.reset();//重置
        			canvas.drawLine(startX, startY, stopX, stopY, paint);
    			}/*else if(i == 0 && isliga){
    				startX =  lineDatas.get(count-1).startX;
            		startY = lineDatas.get(count-1).startY;
            		paint.reset();//重置
        			canvas.drawLine(startX, startY, stopX, stopY, paint);
    			}*/
    			
    		}
    	}
    	if(isliga){  //绘制连线里面的小点的
    		//paintEditorPoint.reset();
    		//paintPoint.reset();//重置  
    		if(!isStart){
    			initLineData();
    			isStart = !isStart;
    		}    			
    		for(int i=0;i<pintLineDatas.size();i++){
    			float startX2 = pintLineDatas.get(i).startX;
    			float startY2  = pintLineDatas.get(i).startY;
    			if(pintLineDatas.get(i).isEditor){
    				canvas.drawCircle(startX2, startY2, 4, paintEditorPoint); 
    			}else{
    				canvas.drawCircle(startX2, startY2, 4, paintPoint); 
    			}    			
    		}
    		if(isStart){
    			if(startlineData != null){
    				float startX2 = startlineData.startX;
        			float startY2  = startlineData.startY;
        			if(startlineData.isEditor){
        				canvas.drawCircle(startX2, startY2, 20, paintEditorPoint); 
        			}else{
        				canvas.drawCircle(startX2, startY2, 20, paintPoint); 
        			}    	
    			}
    		}
    	}
    	
    }  
    
    private void initLineData() {
    	if(lineDatas == null || lineDatas.size()<= 0){
    		return;
    	}
    	LineData rangeData = getPolygonRange(lineDatas,lineDatas.size());
		for(int i1= (int)rangeData.startX;i1<(int)rangeData.endX;i1++){
			if(i1 != 0)
			i1 = i1+9;
			for(int i2 = (int)rangeData.startY;i2<(int)rangeData.endY;i2++){
				LineData point = new LineData();
				point.startX = i1;
				point.startY = i2;
				boolean d = isInPolygon(point,lineDatas,lineDatas.size());
				if(d){
					pintLineDatas.add(point);
				}
				 i2 = i2+9;
			}
		}
		
	}
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction() == MotionEvent.ACTION_DOWN){
    		if(!isliga){
    			float x = event.getX();
    			// TODO Auto-generated method stub
    			float y = event.getY();
        		LineData lineData = new LineData();
        		lineData.startX = x;
        		lineData.startY = y;
        		lineDatas.add(lineData);
        		invalidate(); //重新绘制区域
    		}
		}
    	// TODO Auto-generated method stub
    	return super.onTouchEvent(event);
    }
    public void finishLine(){
    	if(!isliga){
    		isliga = true;
        	invalidate(); //重新绘制区域
    	}
    	
    }
    /**
     * 判断点是否在多边形里
     * @param point
     * @param points
     * @param n
     * @return
     */
    public boolean isInPolygon(LineData point, List<LineData> points, int n) {
        int nCross = 0;
        for (int i = 0; i < n; i++) {
        	LineData p1 = points.get(i);
        	LineData p2 = points.get((i + 1) % n);
            // 求解 y=p.y 与 p1 p2 的交点
            // p1p2 与 y=p0.y平行
            if (p1.startY == p2.startY)
                continue;
            // 交点在p1p2延长线上
            if (point.startY < Math.min(p1.startY, p2.startY))
                continue;
            // 交点在p1p2延长线上
            if (point.startY >= Math.max(p1.startY, p2.startY))
                continue;
            // 求交点的 X 坐标
            double x = (double) (point.startY - p1.startY) * (double) (p2.startX - p1.startX)
                    / (double) (p2.startY - p1.startY) + p1.startX;
            // 只统计单边交点
            if (x > point.startX)
                nCross++;
        }
        return (nCross % 2 == 1);
    }
    /**
     * 获取这个区域内的点集合
     * @param points
     * @param n
     * @return
     */
    public LineData getPolygonRange(List<LineData> points, int n) {
       //计算x.y 最小  x.y最大
    	
    	float minX = 0;
    	float maxX = 0;
    	float minY = 0;
    	float maxY = 0;
    	LineData[] pons = new LineData[n];
    	for (int i = 0; i < points.size(); i++) {
    		pons[i] = points.get(i);
    	}
    	printPons(pons);
    	//比较x的大小
    	LineData temp; // 记录临时中间值   
        int size = pons.length; // 数组大小   
        for (int i = 0; i < size - 1; i++) {   
            for (int j = i + 1; j < size; j++) {   
                if (pons[i].startX < pons[j].startX) { // 交换两数的位置   
                    temp = pons[i];   
                    pons[i] = pons[j];   
                    pons[j] = temp;   
                }   
            }   
        }   
 
    	if(pons[n-1].startX < pons[0].startX){
    		minX = pons[n-1].startX;
        	maxX = pons[0].startX;
    	}else{
    		maxX = pons[n-1].startX;
    		minX = pons[0].startX;
    	}
    	
    	//比较y的大小
    	for (int i = 0; i < size - 1; i++) {   
            for (int j = i + 1; j < size; j++) {   
                if (pons[i].startY < pons[j].startY) { // 交换两数的位置   
                    temp = pons[i];   
                    pons[i] = pons[j];   
                    pons[j] = temp;   
                }   
            }   
        }   
 
    	if(pons[n-1].startY < pons[0].startY){
    		minY = pons[n-1].startY;
        	maxY = pons[0].startY;
    	}else {
    		maxY = pons[n-1].startY;
    		minY = pons[0].startY;
		}
    	
    	
    	LineData lineData = new LineData();
    	lineData.startX = minX;
    	lineData.startY = minY;
    	
    	lineData.endX = maxX;
    	lineData.endY = maxY;
    	
    	return lineData;
    }
    private void printPons(LineData[] pons){
    	for(int i = 0;i<pons.length;i++){
    		Log.i("CustomLineView", "x:"+pons[i].startX+",y:"+pons[i].startY);
    	}
    }
    private LineData startlineData;
    public void startWork(LineData lineData){
    	isStart = true;
    	if(isliga && isStart){
    		if(lineData != null){
            	for(int i = 0;i<pintLineDatas.size();i++){
            		LineData od = pintLineDatas.get(i);
            		float odx =  od.startX;	
            		float ody =  od.startY;	
            		
            		float x = lineData.startX;	
            		float y = lineData.startY;	
            		if((odx<=x+5 && odx>x-5)){
            			if(ody<=y+5 && ody>y-5){
            				startlineData = pintLineDatas.get(i);
            				pintLineDatas.get(i).isEditor = true;
            			}
            		}
            	}
            	invalidate(); //重新绘制区域
        	}
    	}
    }
	public boolean isFinish() {
		return isliga;
	}
    
}
