package MutsuRobot;

class CircularEnemyRad extends Enemy
{
    final double PI = Math.PI;
    public double changeHead;
    
    //敵は直線運動か、円形運動かの判定
    public void setNextXY(long when, double x0, double y0, double bulletSpeed)
    {
        double diff = when - checkTime;
        //double newX, newY;
        //敵の動きで円形予測か直線予測か判定
        if(Math.abs(changeHead) > 0.0001) {circular(diff);}
        else {liner(diff, x0, y0, bulletSpeed);}
    }
    
    //円形予測の計算
    private void circular(double diff)
    {
        double radius = speed / changeHead;
        double toHead = diff * changeHead;
        nextX = x + (Math.cos(head) * radius) - (Math.cos(head + toHead) * radius);
        nextY = y + (Math.sin(head + toHead) * radius) - (Math.sin(head) * radius);
    }
    
    //直線予測の計算
    private void liner(double diff, double x0, double y0, double bulletSpeed)
    {
        double dX = x - x0;
        double dY = y - y0;
        double targetOwnHeading = PI / 2 - head;
        double vX = Math.cos(targetOwnHeading) * speed; //敵の進路ベクトルx成分
        double vY = Math.sin(targetOwnHeading) * speed; //敵の進路ベクトルy成分
        
        double A = Math.pow(vX, 2) + Math.pow(vY, 2) - Math.pow(bulletSpeed, 2);   // Ax^2 + Bx + C = 0
        double B = (2 * vX * dX) + (2 * vY * dY);
        double C = Math.pow(dX, 2) + Math.pow(dY, 2);
        
        double t1, t2;
        if(Math.pow(B, 2) > 4 * A * C)
        {
            t1 = (-B - Math.sqrt(Math.pow(B, 2) - (4 * A * C))) / (2 * A); //二次方程式解の公式
            t2 = (-B + Math.sqrt(Math.pow(B, 2) - (4 * A * C))) / (2 * A);
            if(t1 < 0) {t1 = t2 + 1;}
            if(t2 < 0) {t2 = t1 + 1;}
            if(t1 > t2) {t1 = t2;}  //0以上で小さい方の値を使う
            nextX = x + (vX * t1);
            nextY = y + (vY * t1);
        }
        else
        {
            nextX = x + (vX * diff);
            nextY = y + (vY * diff);
        }
    }
}