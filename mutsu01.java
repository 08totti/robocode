package MutsuRobot;

import java.awt.*;
import java.util.*;
import robocode.*;
import robocode.util.Utils;

/**
 *
 * @author test
 */

public class mutsu01 extends AdvancedRobot
{
    Hashtable targets = new Hashtable(); //敵の情報管理クラスを集中管理するクラス
    
    CircularEnemyRad target = new CircularEnemyRad();
    Direction movDir = new Direction(); //進行方向を管理
    Direction sweepDir = new Direction(); //レーダー方向を管理
    Rad rad = new Rad();
    
    final double PI = Math.PI;
    double sweepKaku = PI;
    double firePower;
    double abs_bearing_rad;
    double bearing;
    
    @Override
    public void run()
    {
        setBodyColor(Color.pink);
        setGunColor(Color.pink);
        setRadarColor(Color.pink);
        setScanColor(Color.pink);
        setBulletColor(Color.pink);
        
        setAdjustRadarForGunTurn(true); //大砲が回転するときに、レーダーが自動的に反対方向に回転するように設定
        setAdjustRadarForRobotTurn(true); //ロボットが回転するときに、レーダーが自動的に反対方向に回転するように設定
        setAdjustGunForRobotTurn(true); //ロボットが回転するときに、大砲が自動的に反対方向に回転するように設定
        
        //開始後バトルフィールドの中央に移動
        /*
        double defX = getBattleFieldWidth() / 2 - getX();
        double defY =  getBattleFieldHeight() / 2 - getY();
        double distance = Math.sqrt(Math.pow(defX, 2) + Math.pow(defY, 2));
        double kaku = PI / 2 - Math.atan2(defY, defX);
        kaku = getHeadingRadians() - kaku;
        turnLeftRadians(rad.normalAngle(kaku));
        ahead(distance);
        */

        turnRadarRightRadians(2 * PI);  //索敵
        
        while(true)
        {
            if(target.energy > 0)
            {               
                movement();
                scanner();
                gun();
                if(firePower != 0){fire(firePower);}
            }
            else
            {
                RamBonus();
            }
            execute();  //非ブロッキングメソッドを実行
        }
    }
    
    void movement()
    {
        GravPoint p;
        Enemy en;
        Enumeration e = targets.elements();
        
        double xforce = 0;
        double yforce = 0;
        double force;
        double ang;
        double moveDistance;
        double changeInEnergy = target.previousEnergy - target.energy;
        double fieldWidth = getBattleFieldWidth();
        double fieldHeight = getBattleFieldHeight();
        int borderRange = getSentryBorderSize() - 20;
        boolean horizontal = false;
        boolean vertical = false;
        
        if(getY() < borderRange || getY() > fieldHeight - borderRange){horizontal = true;}
        if(getX() < borderRange || getX() > fieldWidth - borderRange){vertical = true;}        
        
        if(horizontal || vertical)
        {
            while (e.hasMoreElements()) 
            {
                en = (Enemy)e.nextElement();
                if (en.live && !"samplesentry.BorderGuard".equals(en.name)); 
                {
                    //生存している敵に-1000の斥力を与える
                    final double ENEMYFORCE = -1000;
                    p = new GravPoint(en.x,en.y, ENEMYFORCE);
                    force = p.power/Math.pow(rad.getRange(getX(),getY(),p.x,p.y),2);
                    ang = rad.normalAngle(PI/2 - Math.toDegrees(Math.atan2(getY() - p.y, getX() - p.x))); 
                    xforce += Math.sin(Math.toRadians(ang)) * force;
                    yforce += Math.cos(Math.toRadians(ang)) * force;
                }
            }
            
            //壁からの反発力を3乗で加算
            //final double WALLFORCE = 5000;
            final double WALLFORCE = 5 * (fieldWidth + fieldHeight);
            xforce += WALLFORCE / Math.pow(fieldWidth - getX(), 3);
            xforce -= WALLFORCE / Math.pow(getX(), 3);
            yforce += WALLFORCE / Math.pow(fieldHeight - getY(), 3);
            yforce -= WALLFORCE / Math.pow(getY(), 3);
            
            /*
            xforce += WALLFORCE / Math.pow(rad.getRange(getX(), getY(), fieldWidth - borderRange, getY()), 3);
            xforce -= WALLFORCE / Math.pow(rad.getRange(getX(), getY(), borderRange, getY()), 3);
            yforce += WALLFORCE / Math.pow(rad.getRange(getX(), getY(), getX(), fieldHeight - borderRange), 3);
            yforce -= WALLFORCE / Math.pow(rad.getRange(getX(), getY(), getX(), borderRange), 3);
            */
            
            //角からの反発力を加算
            double cornerForce;
            cornerForce = -0.005 * (fieldWidth + fieldHeight) / rad.getRange(getX(), getY(), 0, 0);
            ang = rad.normalAngle(PI / 2 - Math.toDegrees(Math.atan2(getY(), getX())));
            xforce += Math.sin(Math.toRadians(ang)) * cornerForce;
            yforce += Math.cos(Math.toRadians(ang)) * cornerForce;
			
            cornerForce = -0.005 * (fieldWidth + fieldHeight) / rad.getRange(getX(), getY(), fieldWidth, 0);
            ang = rad.normalAngle(PI / 2 - Math.toDegrees(Math.atan2(getY(), getX() - fieldWidth)));
            xforce += Math.sin(Math.toRadians(ang)) * cornerForce;
            yforce += Math.cos(Math.toRadians(ang)) * cornerForce;
			
            cornerForce = -0.005 * (fieldWidth + fieldHeight) / rad.getRange(getX(), getY(),fieldWidth, fieldHeight);
            ang = rad.normalAngle(PI / 2 - Math.toDegrees(Math.atan2(getY() - fieldHeight, getX() - fieldWidth)));
            xforce += Math.sin(Math.toRadians(ang)) * cornerForce;
            yforce += Math.cos(Math.toRadians(ang)) * cornerForce;
		
            cornerForce = -0.005 * (fieldWidth + fieldHeight) / rad.getRange(getX(), getY(), 0, fieldHeight);
            ang = rad.normalAngle(PI / 2 - Math.toDegrees(Math.atan2(getY() - fieldHeight, getX())));
            xforce += Math.sin(Math.toRadians(ang)) * cornerForce;
            yforce += Math.cos(Math.toRadians(ang)) * cornerForce;
        
            //方向転換する代わりに後退するよう角度を補正
            double kaku = getHeadingRadians() + Math.atan2(yforce, xforce) - (PI / 2); //移動設定
            int dir;
            if(kaku > PI / 2)
            {
                kaku -= PI;
                dir = -1;
            }
            else if(kaku < -PI / 2)
            {
                kaku += PI;
                dir = -1;
            }
            else
            {
                dir = 1;
            }
            
            //敵のエネルギーが変化していた時だけ移動する
            if(changeInEnergy != 0)
            {
                //距離に応じて移動量を変化
                if(target.distance > 400)
                {
                    moveDistance = 100;
                }
                else
                {
                    moveDistance = 300;
                }
                setTurnRightRadians(kaku);
                setAhead(moveDistance * dir);
            }
        }
        else
        {
            //敵のエネルギーが変化していた時だけ移動する
            if(changeInEnergy != 0)
            {
                //距離に応じて移動量を変化
                if(target.distance > 400)
                {
                    moveDistance = 100;
                }
                else
                {
                    moveDistance = 300;
                }
                setAhead(moveDistance * movDir.getDir());
            }
            setTurnRightRadians((PI / 2) + target.bearing);
        }
    }
    
    void scanner()
    {
        if(sweepKaku > PI / 2)
        {
            sweepKaku = PI * 2;
        }
        else
        {
            sweepKaku += PI / 8;
            sweepDir.flip();
        }
        setTurnRadarRightRadians(sweepKaku * sweepDir.getDir());
    }
    
    void gun()
    {
        //砲塔を予測地点に向ける      
        double bulletSpeed = 20 - (3 * firePower);
        long nextTime = (int)Math.round(target.distance / bulletSpeed);
        long time = getTime() + nextTime;
        target.setNextXY(time, getX(), getY(), bulletSpeed);
        double nextX = target.nextX - getX();
        double nextY = target.nextY - getY();
        double kaiten = PI / 2 - Math.atan2(nextY, nextX);
        setTurnGunRightRadians(rad.normalAngle(kaiten - getGunHeadingRadians()));
        //予測地点が、バトルフィールド内の場合攻撃
        if((target.nextX > 0) && (target.nextY > 0) && (target.nextX < getBattleFieldWidth()) && (target.nextY < getBattleFieldHeight()))
        {
            firePower();
        }       
    }
    
    void firePower()
    {
        //弾のパワー値を設定
        if(getEnergy() < 10)
        {
            if(getEnergy() <= 1){firePower = 0;}
            else{firePower = getEnergy() * 0.1;}
        }
        else if(target.distance < 30 || target.speed == 0)
        {
            firePower = 3;
        }
        else
        {
            if(target.energy < 5){firePower = getEnergy() * 0.1;}
            else{firePower = 500 / target.distance;}
        }
        //3以上は3に固定
        if(firePower > 3){firePower = 3;}
        if("samplesentry.BorderGuard".equals(target.name)){firePower = 0;}
    }
    
    //絶対角度hdからgdへの相対角度を返す。返す値は-180~180
    public double getRelAngle(double gd, double hd)
    {
        double kd = gd - hd;
        if(kd < -PI)
        {
            return kd + (2 * PI);
        }
        else if(kd > PI)
        {
            return kd - (2 * PI);
        }
        return kd;
    }
    
    void RamBonus()
    {
        //RamBonusのために、敵がDisabledになったら体当たり
        double enemyAngle = target.bearing + getHeadingRadians(); // 敵までの絶対角度
        double kd = getRelAngle(enemyAngle, getHeadingRadians());
        turnRadarRightRadians(getHeadingRadians());
        turnRightRadians(kd);
        ahead(target.distance);
        turnRadarRightRadians(2 * PI);
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent e)
    {
        //敵ロボットの情報をセット
        bearing = e.getBearing();
        double h = rad.normalAngle(e.getHeadingRadians() - target.head);
        h = h / (getTime() - target.checkTime);
        target.changeHead = h;
        abs_bearing_rad = (getHeadingRadians() + target.bearing % (2 * PI)); //敵ロボットの方向を求める

        target.x = getX() + Math.sin(abs_bearing_rad) * e.getDistance(); //敵ロボットの座標を取得
        target.y = getY() + Math.cos(abs_bearing_rad) * e.getDistance();
        
        target.name = e.getName();
        target.bearing = e.getBearingRadians();
        target.head = e.getHeadingRadians();
        target.speed = e.getVelocity();
        target.distance = e.getDistance();
        target.checkTime = getTime();   //情報を記録した時間
        target.setEnergy(e.getEnergy());    //敵ロボットのエネルギーを記録
        sweepKaku = PI / 10;    //レーダーの往復量をリセット
    }
    
    @Override
    public void onHitByBullet(HitByBulletEvent e)
    {
        turnRadarRightRadians(rad.normalAngle(getHeadingRadians() + target.bearing - getRadarHeadingRadians()));
   
        //デバッガー
        setDebugProperty("lastHitBy", e.getName() + " with power of bullet " + e.getPower() + " at time " + getTime());
	setDebugProperty("lastScannedRobot", null);

	Graphics2D g = getGraphics();
	g.setColor(Color.orange);
	g.drawOval((int) (getX() - 55), (int) (getY() - 55), 110, 110);
        g.drawOval((int) (getX() - 56), (int) (getY() - 56), 112, 112);
	g.drawOval((int) (getX() - 59), (int) (getY() - 59), 118, 118);
        g.drawOval((int) (getX() - 60), (int) (getY() - 60), 120, 120);
    }
    
    @Override
    public void onHitWall(HitWallEvent e)
    {

    }
            
    @Override
    public void onPaint(Graphics2D g)
    {
        //デバッガー
        /*
        int laserLength = 200;
        double heading = getGunHeadingRadians();
        double x = Math.sin(Math.toRadians(heading)) * laserLength;
        double y = Math.cos(Math.toRadians(heading)) * laserLength;
        
        g.setColor(Color.pink);
        g.drawLine((int)getX(), (int)getY(), (int)(x + getX()), (int)(y + getY()));
        */
        g.setColor(Color.red);
        g.drawOval((int) (getX() - 50), (int) (getY() - 50), 100, 100);
	g.setColor(new Color(0, 0xFF, 0, 30));
	g.fillOval((int) (getX() - 60), (int) (getY() - 60), 120, 120);
    }
}

