package fr.ign.cogit.simplu3d.rjmcmc.cuboid.energy.cuboid2;

import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.impl.Cuboid2;
import fr.ign.rjmcmc.energy.UnaryEnergy;

public class VolumeUnaryEnergy<T> implements UnaryEnergy<T> {

  @Override
  public double getValue(T t) {


    if (!(t instanceof Cuboid2)) {
      System.out.println("Probleme : volume unary energy");
      return 0;
    }
    Cuboid2 c = (Cuboid2) t;

    
    double volume= c.width * c.length * c.height;

    return volume;

  }

}
