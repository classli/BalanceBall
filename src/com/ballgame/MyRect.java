package com.ballgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class MyRect{
	private Bitmap bmp;
	private float x,y;
	
	public MyRect(Bitmap bmp,float x,float y){
		this.bmp=bmp;
		this.x=x;
		this.y=y;
	}
	
	public void draw(Canvas canvas, Paint paint){
		canvas.drawBitmap(bmp, x, y, paint);
	}

	public void setX(float x) {
		this.x = x;
	}	

	public void setY(float y) {
		this.y = y;
	}
	
	public int getW(){
		return bmp.getWidth();
	}
	
	public int getH(){
		return bmp.getHeight();				
	}
}