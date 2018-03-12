package MutsuRobot;

/**
 *
 * @author test
 */
public class Enemy
{
    String name;
    public boolean live;
    public double bearing;
    public double head;
    public long checkTime; 	//スキャンしてからの時間
    public double speed;
    public double x,y;
    public double distance;
    public double energy, previousEnergy;
    public double nextX, nextY;
    
    Enemy() //コンストラクタ
    {
        distance = 100000;
        previousEnergy = 100;
    }
    
    public void setNextXY() //ロボットの予測地点を設定
    {
        nextX = x;
        nextY = y;
    }
    
    public void setEnergy(double engy)  //現在とひとつ前のエネルギーレベルを記録
    {
        previousEnergy = energy;
        energy = engy;
    }
}