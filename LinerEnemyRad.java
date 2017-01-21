package final16a;

class LinerEnemyRad extends Enemy 
{
    final double PI = Math.PI;
    
    public boolean setNextXY(double x0, double y0) 
    {
    	boolean flag;
        double dX = x - x0;
        double dY = y - y0;

	double targetOwnHeading = PI/2 - head;
	double vX = Math.cos(targetOwnHeading) * speed;
        double vY = Math.sin(targetOwnHeading) * speed;

	double A = Math.pow(vX, 2) + Math.pow(vY, 2) - 289;     // Ax^2 + Bx + C = 0
        double B =(2 * vX * dX) + (2 * vY * dY);
	double C = Math.pow(dX, 2) + Math.pow(dY, 2);
        
        double t1, t2;
	if (Math.pow(B, 2) > 4 * A * C) 
        {
            t1 = (- B - Math.sqrt(Math.pow(B, 2) - (4 * A * C))) / (2 * A);
            t2 = (- B + Math.sqrt(Math.pow(B, 2) - (4 * A * C))) / (2 * A);
            if (t1 < 0) {t1 = t2 + 1;}
            if (t2 < 0) {t2 = t1 + 1;}
            if (t1 > t2) {t1 = t2;}

            nextX = x + (vX * t1);
            nextY = y + (vY * t1);	
            flag = true;	
	}
        else 
        {
            flag = false;
	}
        return flag;
    }
}
