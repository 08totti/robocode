package MutsuRobot;

/**
 *
 * @author test
 */
class GravPoint 
{
    //重力ポイントのX-Y座標と力を格納するクラス
    public double x, y, power;
    
    public GravPoint(double pX, double pY, double pPower)
    {
        x = pX;
        y = pY;
        power = pPower;
    }
}
