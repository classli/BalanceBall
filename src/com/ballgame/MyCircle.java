package com.ballgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class MyCircle{
	
	float x,y,r,angle;
	private Bitmap bmp;
	
	public MyCircle(Bitmap bmp, float x, float y ,float r){
		this.x=x;
		this.y=y;
		this.r=r;
		this.bmp=bmp;
	}
	
	public void draw(Canvas canvas, Paint paint){
		canvas.save();
		canvas.rotate(angle, x+r, y+r);
		canvas.drawBitmap(bmp, x, y, paint);
		canvas.restore();
	}

	public float getR() {
		return r;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}
	
	
	
	
}