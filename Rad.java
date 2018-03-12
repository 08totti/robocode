package MutsuRobot;

/**
 *
 * @author test
 */
class Rad 
{
    final double PI = Math.PI;
    
    public double normalAngle(double kaku)
    {
        //180度以上回転する場合を補正
        if(kaku > PI)
        {
            kaku -= PI * 2;
        }
        if(kaku < -PI)
        {
            kaku += PI * 2;
        }
        return kaku;
    }
    
    //2点間の直線距離を求める
    public double getRange(double x1, double y1, double x2, double y2)
    {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double h = Math.sqrt(xo * xo + yo * yo);
        return h;
    }
}
