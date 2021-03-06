package fr.ign.cogit.simplu3d.test.rjmcmc.cuboid.transformation.birth;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.impl.Cuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.transformation.birth.TransformToSurface;
import fr.ign.random.Random;

/**
 * 
 * This software is released under the licence CeCILL
 * 
 * see LICENSE.TXT
 * 
 * see  http://www.cecill.info/
 * 
 * 
 * 
 * copyright IGN
 * 
 * @author Brasebin Mickaël
 * 
 * @version 1.0
 **/
public class TransformToSurfaceTest {
  TransformToSurface t;

  public static void main(String[] args) throws Exception {
	  TransformToSurfaceTest test = new TransformToSurfaceTest();
	  test.setUp();
	  test.testApply();
  }
  @Before
  public void setUp() throws Exception {
    IPolygon polygon = (IPolygon) WktGeOxygene
        .makeGeOxygene("POLYGON (( 5 0, 5 -10, 15 -10, 15 -5, 10 -5, 10 0, 5 0 ))");
    double[] d = new double[] { 0, 0, 1, 1, 3, 0 };
    double[] v = new double[] { 10, 10, 5, 5, 3, Math.PI };
    t = new TransformToSurface(d, v, polygon);
  }

  @Test
  public void testApply() {

    int dimension = t.dimension() - 2;

    double[] in = new double[] { 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 };
    double[] out = new double[t.dimension()];

    // Vector<Double> lvalIn = new Vector<Double>();

    // lvalIn.add(0.5);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);

    // Vector<Double> lvarIn = new Vector<Double>();
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    //
    // Vector<Double> lvalOut = new Vector<>();
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);

    // Vector<Double> lvarOut = new Vector<>();
    // lvarOut.add(0.0);
    // lvarOut.add(0.0);

    t.apply(true, in, out);

    String outString = "";

    for (int i = 0; i < dimension; i++) {
      outString += out[i] + " ";
    }

    // outString += lvarOut.get(0) + " ";
    // outString += lvarOut.get(1) + " ";

    System.out.println("out = " + outString);

    Cuboid cuboidOut = new Cuboid(out[0], out[1], out[2], out[3], out[4], out[5]);

    System.out.println("Out = " + cuboidOut.toGeometry());
    /*
     * in = new double[] { 0, 0, 0.5, 0.5, 0.5, 0.5, 0.5 }; t.apply(in, out);
     */
    in = new double[] { 0, 0, 0.5, 0.5, 0.5, 0.5, 0.5 };
    // lvalIn = new Vector<Double>();

    // lvalIn.add(0.0);
    // lvalIn.add(0.0);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);
    // lvalOut.add(0.5);

    // lvarIn = new Vector<Double>();
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    //
    // lvalOut = new Vector<>();
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);

    // lvarOut = new Vector<>();
    // lvarOut.add(0.0);
    // lvarOut.add(0.0);

    t.apply(true, in, out);
    outString = "";

    for (int i = 0; i < dimension; i++) {
      outString += out[i] + " ";
    }

    // outString += lvarOut.get(0) + " ";
    // outString += lvarOut.get(1) + " ";

    System.out.println("out = " + outString);
    cuboidOut = new Cuboid(out[0], out[1], out[2],
        out[3], out[4], out[5]);
    System.out.println("Out = " + cuboidOut.toGeometry());

    in = new double[] { 1, 1, 0.5, 0.5, 0.5, 0.5, 0.5 };
    // lvalIn.clear();
    // lvalIn.add(1.0);
    // lvalIn.add(1.0);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);
    // lvalIn.add(0.5);

    // lvarIn.clear();
    // lvarIn.add(1.0);
    // lvarIn.add(1.0);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    // lvarIn.add(0.5);
    //
    // lvalOut.clear();
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);
    // lvalOut.add(0.0);

    // lvarOut.clear();
    // lvarOut.add(0.0);
    // lvarOut.add(0.0);

    t.apply(true, in, out);

    // t.apply(in, out);
    outString = "";
    outString = "";

    for (int i = 0; i < dimension; i++) {
      outString += out[i] + " ";
    }

    // outString += lvarOut.get(0) + " ";
    // outString += lvarOut.get(1) + " ";

    System.out.println("out = " + outString);
    cuboidOut = new Cuboid(out[0], out[1], out[2],
        out[3], out[4], out[5]);
    System.out.println("Out = " + cuboidOut.toGeometry());

    RandomGenerator generator = Random.random();
    GeometryFactory f = new GeometryFactory();
    for (int i = 0; i < 1; i++) {

      in = new double[] { generator.nextDouble(), generator.nextDouble(), 0.5, 0.5, 0.5, 0.5, 0.5 };
      // lvalIn = new Vector<Double>();

      // lvalIn.add(generator.nextDouble());
      // lvalIn.add(generator.nextDouble());
      // lvalIn.add(0.5);
      // lvalIn.add(0.5);
      // lvalIn.add(0.5);
      // lvalIn.add(0.5);

      // lvarIn = new Vector<Double>();
      // lvarIn.add(generator.nextDouble());
      // lvarIn.add(generator.nextDouble());
      // lvarIn.add(0.5);
      // lvarIn.add(0.5);
      // lvarIn.add(0.5);
      // lvarIn.add(0.5);

      // lvalOut = new Vector<>();
      // lvalOut.add(0.0);
      // lvalOut.add(0.0);
      // lvalOut.add(0.0);
      // lvalOut.add(0.0);
      // lvalOut.add(0.0);
      // lvalOut.add(0.0);
      //
      // lvarOut = new Vector<>();
      // lvarOut.add(0.0);
      // lvarOut.add(0.0);

      t.apply(true, in, out);

      System.out.println(f.createPoint(new Coordinate(out[0], out[1])));

    }
  }

  @Test
  public void testInverse() {
    // fail("Not yet implemented"); // TODO
  }

  @Test
  public void testDimension() {
    // fail("Not yet implemented"); // TODO
  }

}
