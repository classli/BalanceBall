package com.ballgame;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.provider.CalendarContract.Instances;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements Callback,Runnable,ContactListener{

	private Thread thd;
	private Canvas canvas;
	private Paint paint;
	private SurfaceHolder sfh;
	private boolean flag;
	private int screenW;
	private int screenH;
	//=======================//
	private World world;
	private final float RATE=30;
	private AABB aabb;
	private Vec2 gravity;
	private float timestep=1.0f/60.0f;//模拟世界的频率
	private int iterations=10;//迭代值
	//==========================//
	private final int GAMESTATE_MENU=0;
	private final int GAMESTATE_HELP=1;
	private final int GAMESTATE_GAMEING=2;
	private  int gameState=GAMESTATE_MENU;
	//===========================//
	private Bitmap bmpMenu,bmpMenu_help, bmpMenu_play, bmpMenu_exit,bmpMenuBack,
	bmp_helpbg,bmp_gamebg,bmpH, bmpS, bmpSh, bmpSs, bmpBall,bmpBody_lost,bmpBody_win,
	bmp_smallbg,bmpMenu_resume,bmpMenu_replay,bmpMenu_menu,bmpWinbg, bmpLostbg;
	//==========================//
	private HButton hbHelp, hbPlay, hbExit, hbResume, hbReplay, hbBack, hbMenu;
	//=======================//
	private Body bodyBall;
	private Body lostBody1, lostBody2, winBody;
	private boolean gameIsPause, gameIsLost, gameIsWin;
	//===========================//
	private SensorManager sm;
	//声明一个传感器
	private Sensor sensor;
	//声明一个传感器监听器
	private SensorEventListener mySensorListener;
	private float ax,ay,acx,acy;
	private Vibrator vibrator;
	
	public MySurfaceView(Context context) {
		super(context);
		this.setKeepScreenOn(true);
		sfh=this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		//=================================//
		aabb = new AABB();
		aabb.lowerBound.set(-100, -100);
		aabb.upperBound.set(100, 100);
		gravity = new Vec2(0,10);
		world = new World(aabb, gravity, true);
		//=============================//
		vibrator=(Vibrator)MainActivity.main.getSystemService(Service.VIBRATOR_SERVICE);
		sm=(SensorManager)MainActivity.main.getSystemService(Service.SENSOR_SERVICE);
		sensor=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mySensorListener = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				// TODO Auto-generated method stub
				ax=event.values[0];
				ay=event.values[1];
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		sm.registerListener(mySensorListener, sensor, GAMESTATE_GAMEING);
		initGmae();
	}
	
	public void initGmae(){
		bmpMenu = BitmapFactory.decodeResource(this.getResources(), R.drawable.menu_bg);	
		bmpMenu_help = BitmapFactory.decodeResource(getResources(), R.drawable.menu_help);
		bmpMenu_play = BitmapFactory.decodeResource(getResources(), R.drawable.menu_play);
		bmpMenu_exit = BitmapFactory.decodeResource(getResources(), R.drawable.menu_exit);
		bmpMenuBack = BitmapFactory.decodeResource(getResources(), R.drawable.menu_back);
		bmp_helpbg = BitmapFactory.decodeResource(getResources(), R.drawable.helpbg);
		bmp_gamebg = BitmapFactory.decodeResource(getResources(), R.drawable.game_bg);
		bmpH = BitmapFactory.decodeResource(getResources(), R.drawable.h);
		bmpS = BitmapFactory.decodeResource(getResources(), R.drawable.s);
		bmpSh = BitmapFactory.decodeResource(getResources(), R.drawable.sh);
		bmpSs = BitmapFactory.decodeResource(getResources(), R.drawable.ss);
		bmpBall = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		bmpBody_lost = BitmapFactory.decodeResource(getResources(), R.drawable.lostbody);
		bmpBody_win = BitmapFactory.decodeResource(getResources(), R.drawable.winbody);
		bmp_smallbg = BitmapFactory.decodeResource(getResources(), R.drawable.smallbg);
		bmpMenu_resume = BitmapFactory.decodeResource(getResources(), R.drawable.menu_resume);
		bmpMenu_replay = BitmapFactory.decodeResource(getResources(), R.drawable.menu_replay);
		bmpMenu_menu = BitmapFactory.decodeResource(getResources(), R.drawable.menu_menu);
		bmpWinbg = BitmapFactory.decodeResource(getResources(), R.drawable.gamewin);
		bmpLostbg = BitmapFactory.decodeResource(getResources(), R.drawable.gamelost);
	}
	
	@Override
	public void run() {
		while(flag){
			myDraw();
			logic();
			try {
				Thread.sleep((long)timestep*1000);
			} catch (InterruptedException e) {
				Log.e("Himi", "========myDraw error==============");
				e.printStackTrace();
			}
		}
	}

	public Body creatRect(Bitmap bmp, float x, float y ,float w, float h, float density){
		PolygonDef plf=new PolygonDef();
		plf.density=density;
		plf.friction=0.8f;
		plf.restitution=0.3f;
		plf.setAsBox(w/2/RATE, h/2/RATE);
		BodyDef bd= new BodyDef();
		bd.position.set((x+w/2)/RATE, (y+h/2)/RATE);
		Body body=world.createBody(bd);
		body.m_userData=new MyRect(bmp, x, y);
		body.createShape(plf);
		body.setMassFromShapes();
		return body;
	}
	
	public Body creatCircle(Bitmap bmp, float x, float y, float r, float density){
		CircleDef cf=new CircleDef();
		cf.density=density;
		cf.friction=0.8f;
		cf.restitution=0.1f;
		cf.radius=r/RATE;
		BodyDef bd=new BodyDef();
		bd.position.set((x+r)/RATE, (y+r)/RATE);
		Body body = world.createBody(bd);
		body.m_userData=new MyCircle(bmp, x, y, r);
		body.createShape(cf);
		body.setMassFromShapes();
		body.allowSleeping(flag);
		return body;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(gameState==GAMESTATE_MENU){
			screenW=this.getWidth();
			screenH=this.getHeight();
			hbPlay = new HButton(bmpMenu_play, screenW/2 - bmpMenu_help.getWidth()/2, screenH/2 - bmpMenu_help.getHeight()/ 2);
			hbHelp = new HButton(bmpMenu_help, hbPlay.getX(), hbPlay.getY() + 150);
			hbExit = new HButton(bmpMenu_exit, hbPlay.getX(), hbHelp.getY() + 150);
			hbBack = new HButton(bmpMenuBack, 0, screenH - bmpMenu_help.getHeight());
			hbResume = new HButton(bmpMenu_resume, screenW / 2 - bmpMenu_resume.getWidth() / 2, screenH / 2 - bmpMenu_resume.getHeight()-50);
			hbReplay = new HButton(bmpMenu_replay, hbResume.getX(), hbResume.getY() + 120);
			hbMenu = new HButton(bmpMenu_menu, hbResume.getX(), hbReplay.getY() + 120);
			bodyBall = creatCircle(bmpBall, bmpH.getHeight(), bmpH.getHeight(), bmpBall.getWidth() / 2, 5);
			lostBody1 = creatCircle(bmpBody_lost, screenW - bmpH.getHeight() - bmpBody_lost.getWidth(), bmpH.getHeight(), bmpBody_lost.getWidth() / 2, 0);
			lostBody2 = creatCircle(bmpBody_lost, bmpH.getHeight(), screenH - bmpH.getHeight() - bmpBody_lost.getHeight(), bmpBody_lost.getWidth() / 2, 0);
			winBody = creatCircle(bmpBody_win, screenW - bmpH.getHeight() - bmpBody_win.getWidth(), screenH - bmpH.getHeight() - bmpBody_win.getHeight(),
					bmpBody_win.getWidth() / 2, 0);
			lostBody1.getShapeList().m_isSensor=true;
			lostBody2.getShapeList().m_isSensor=true;
			winBody.getShapeList().m_isSensor=true;
			creatRect(bmpH, 0, 0, bmpH.getWidth(), bmpH.getHeight(), 0);// 上
			creatRect(bmpH, 0, screenH-bmpH.getHeight(), bmpH.getWidth(), bmpH.getHeight(), 0);// 下
			creatRect(bmpS, 0, 0, bmpS.getWidth(), bmpS.getHeight(), 0);// 左
			creatRect(bmpS, getWidth() - bmpS.getWidth(), 0, bmpS.getWidth(), bmpS.getHeight(), 0);// 右
			creatRect(bmpSh, 0, 190, bmpSh.getWidth(), bmpSh.getHeight(), 0);
			creatRect(bmpSh, 310, 470, bmpSh.getWidth(), bmpSh.getHeight(), 0);
			creatRect(bmpSs, 310, 470, bmpSs.getWidth(), bmpSs.getHeight(), 0);
			creatRect(bmpSs, getWidth() - 220, screenH - bmpSs.getHeight(), bmpSs.getWidth(), bmpSs.getHeight(), 0);
			world.setContactListener(this);
		}
		flag=true;
		thd=new Thread(this);
		thd.start();
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		flag=false;
	}
	
	public void myDraw(){
		try {
			canvas=sfh.lockCanvas();
			if(canvas!=null){
				canvas.drawColor(Color.BLACK);
				switch(gameState){
				case GAMESTATE_MENU:
					canvas.drawBitmap(bmpMenu, 0, 0, paint);
					hbPlay.draw(canvas, paint);
					hbHelp.draw(canvas, paint);
					hbExit.draw(canvas, paint);
					break;
				case GAMESTATE_HELP:
					canvas.drawBitmap(bmp_helpbg, 0, 0, paint);
					hbBack.draw(canvas, paint);
					break;
				case GAMESTATE_GAMEING:
					canvas.drawBitmap(bmp_gamebg, 0, 0, paint);
					Body body = world.getBodyList();
					for(int i=1; i<world.getBodyCount();i++){
						if((body.m_userData) instanceof MyRect){
							MyRect rect = (MyRect) (body.m_userData);
							rect.draw(canvas, paint);	
						}
						else if(body.m_userData instanceof MyCircle){
							MyCircle mcc = (MyCircle) (body.m_userData);
							mcc.draw(canvas, paint);
						}
						body=body.m_next;
					}
					
					if (gameIsPause || gameIsLost || gameIsWin) {
						// 当游戏暂停或失败或成功时画一个半透明黑色矩形，突出界面
						Paint paintB = new Paint();
						paintB.setAlpha(0x77);
						canvas.drawRect(0, 0, screenW, screenH, paintB);
					}
					if (gameIsPause) {
						//绘制暂停背景
						canvas.drawBitmap(bmp_smallbg, screenW / 2 - bmp_smallbg.getWidth() / 2, screenH / 2 - bmp_smallbg.getHeight() / 2, paint);
						//绘制Resume按钮
						hbResume.draw(canvas, paint);
						//绘制Replay按钮
						hbReplay.draw(canvas, paint);
						//绘制Menu按钮
						hbMenu.draw(canvas, paint);
					}
					else if (gameIsLost) {
							canvas.drawBitmap(bmpLostbg, screenW / 2 - bmpLostbg.getWidth() / 2, screenH / 2 - bmpLostbg.getHeight() / 2, paint);
							//绘制Replay按钮
							hbReplay.draw(canvas, paint);
							//绘制Menu按钮
							hbMenu.draw(canvas, paint);
					} else if (gameIsWin) {
							canvas.drawBitmap(bmpWinbg, screenW / 2 - bmpWinbg.getWidth() / 2, screenH / 2 - bmpWinbg.getHeight() / 2, paint);
							//绘制Replay按钮
							hbReplay.draw(canvas, paint);
							//绘制Menu按钮
							hbMenu.draw(canvas, paint);
					}
					break;
				}
				
			}
		} catch (Exception e) {
			Log.e("Himi", "========myDraw error==============");
			e.printStackTrace();
		}finally{
			if(canvas!=null){
				sfh.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	public void logic(){
		
		switch(gameState){
		case GAMESTATE_MENU:
			break;
		case GAMESTATE_HELP:
			break;
		case GAMESTATE_GAMEING:
			if (!gameIsPause && !gameIsLost && !gameIsWin) {
				world.step(timestep, iterations);
				Body body = world.getBodyList();
				for (int i = 1; i < world.getBodyCount(); i++) {
					if ((body.m_userData) instanceof MyRect) {
						MyRect rect = (MyRect) (body.m_userData);
						rect.setX(body.getPosition().x * RATE - rect.getW() / 2);
						rect.setY(body.getPosition().y * RATE - rect.getH() / 2);
					} else if ((body.m_userData) instanceof MyCircle) {
						MyCircle mcc = (MyCircle) (body.m_userData);
						mcc.setX(body.getPosition().x * RATE - mcc.getR());
						mcc.setY(body.getPosition().y * RATE - mcc.getR());
						mcc.setAngle((float) (body.getAngle() * 180 / Math.PI));
					}
					body = body.m_next;
				}
				world.setGravity(new Vec2(-ax*8,ay*8));
			}
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && gameState != GAMESTATE_GAMEING) {
			return true;
		}
		switch(gameState){
		case GAMESTATE_MENU:
			break;
		case GAMESTATE_HELP:
			break;
		case GAMESTATE_GAMEING:
			if (!gameIsPause && !gameIsLost && !gameIsWin) {
				//如果方向键左键被按下
				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					//设置物理世界的重力方向向左 
					world.setGravity(new Vec2(-10, 2));
				//如果方向键右键被按下
				else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					//设置物理世界的重力方向向右
					world.setGravity(new Vec2(10, 2));
				//如果方向键上键被按下
				else if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
					//设置物理世界的重力方向向上
					world.setGravity(new Vec2(0, -10));
				//如果方向键下键被按下
				else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
					//设置物理世界的重力方向向下
					world.setGravity(new Vec2(0, 10));
				//如果返回键被按下
				else if (keyCode == KeyEvent.KEYCODE_BACK) {
					//进入游戏暂停界面
					gameIsPause = true;
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(gameState){
		case GAMESTATE_MENU:
			if (hbPlay.isPressed(event)) {		
				gameState = GAMESTATE_GAMEING;				
			} else if (hbHelp.isPressed(event)) {			
				gameState = GAMESTATE_HELP;
			} else if (hbExit.isPressed(event)) {
				MainActivity.main.exit();
			}
			break;
		case GAMESTATE_HELP:
			if (hbBack.isPressed(event)) {
				gameState=GAMESTATE_MENU;
			}
			break;
		case GAMESTATE_GAMEING:
			if (gameIsPause || gameIsLost || gameIsWin) {
				if (hbResume.isPressed(event)) {
					gameIsPause = false;
				}
				else if (hbReplay.isPressed(event)) {
				bodyBall.putToSleep();
				bodyBall.setXForm(new Vec2((bmpH.getHeight()+bmpBall.getWidth()/2+2)/RATE,(bmpH.getHeight() + bmpBall.getWidth() / 2 + 2) / RATE), 0);
				world.setGravity(new Vec2(0, 10));
				bodyBall.wakeUp();
				gameIsPause = false;
				gameIsLost = false;
				gameIsWin = false;
			}else if (hbMenu.isPressed(event)) {
				bodyBall.putToSleep();//重置前要睡眠
				// 然后对小球的坐标进行重置
				bodyBall.setXForm(new Vec2((bmpH.getHeight() + bmpBall.getWidth() / 2 + 2) / RATE, (bmpH.getHeight() + bmpBall.getWidth() / 2 + 2) / RATE),
						0);
				//并且设置默认重力方向为向下
				world.setGravity(new Vec2(0, 10));
				//唤醒小球
				bodyBall.wakeUp();
				//重置游戏状态为主菜单
				gameState = GAMESTATE_MENU;
				//游戏暂停、胜利、失败条件还原默认false
				gameIsPause = false;
				gameIsLost = false;
				gameIsWin = false;
			}
			}
			break;
		}
		return true;
	}

	@Override
	public void add(ContactPoint arg0) {
		// TODO Auto-generated method stub
		if (gameState == GAMESTATE_GAMEING) {
			if (!gameIsPause && !gameIsLost && !gameIsWin) {
				if(arg0.shape1.getBody()==bodyBall&&arg0.shape2.getBody()==lostBody1)
					gameIsLost=true;
				else if(arg0.shape1.getBody() == bodyBall && arg0.shape2.getBody() == lostBody2){
					gameIsLost=true;
				}
				else if (arg0.shape1.getBody() == bodyBall && arg0.shape2.getBody() == winBody) {
					//游戏胜利
					gameIsWin = true;
				}
				vibrator.vibrate(50);
			}
		}
	}

	@Override
	public void persist(ContactPoint arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(ContactPoint arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void result(ContactResult arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}