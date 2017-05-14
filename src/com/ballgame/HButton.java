package com.ballgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public class HButton{
	private int x,y,w,h;
	private Bitmap bmp;
	
	public HButton(Bitmap bmp, int x ,int y){
		this.x=x;
		this.y=y;
		this.bmp=bmp;
		this.w=bmp.getWidth();
		this.h=bmp.getHeight();
	}
	
	public void draw(Canvas canvas, Paint paint){
		canvas.drawBitmap(bmp, x, y, paint);
	}
	
	public boolean isPressed(MotionEvent event){
		if(event.getAction()== MotionEvent.ACTION_DOWN){
			if(event.getX()<=x+w&&event.getY()>=x){
				if(event.getY()>=y&&event.getY()<=y+h){
					return true;
				}
			}
		}
		return false;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}
	
	
}