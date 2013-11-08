package fr.ign.cogit.simplu3d.exec;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.simplu3d.io.load.application.LoaderSHP;
import fr.ign.cogit.simplu3d.model.application.Environnement;
import fr.ign.cogit.simplu3d.test.rjmcmc.cuboid.OCLBuildingsCuboidFinal;
import fr.ign.cogit.simplu3d.test.rjmcmc.cuboid.geometry.impl.Cuboid2;
import fr.ign.mpp.configuration.Configuration;
import fr.ign.parameters.Parameters;

public class TestParamTemp {

  /**
   * @param args
   */

  // [building_footprint_rectangle_cli_main
  public static void main(String[] args) throws Exception {

    String folderName = "./src/main/resources/scenario/";

    String fileName = "building_parameters_project_expthese_1.xml";

    Parameters p = initialize_parameters(folderName + fileName);


    int nbInter = 20;

    double bMin = 0.99;
    double bMax = 0.9999999;
    
    int count = 0;

    List<Double> ld = new ArrayList<>();

    for (int i = 0; i < nbInter; i++) {

      ld.add(i * (bMax - bMin) / nbInter + bMin);

    }

    ld.add(bMax);

    Object[] valCoeff = ld.toArray();

    int nbIt = 20;


    for (int i = 0; i < valCoeff.length; i++) {

      // writer.append(valCoeff[i] + ";");

      for (int j = 0; j < nbIt; j++) {
        Environnement env = LoaderSHP.load(p.get("folder"));

        OCLBuildingsCuboidFinal ocb = new OCLBuildingsCuboidFinal();
        ocb.setCoeffDec((double) valCoeff[i]);

        double timeMs = System.currentTimeMillis();

        Configuration<Cuboid2> cc = ocb.process(env.getBpU().get(1), p, env, 1);

        
        
        

        System.out.println(valCoeff[i] + "," + ocb.getCount() + ","
            + (System.currentTimeMillis() - timeMs) + "," + cc.getEnergy());
        
        count++;
        
        System.out.println("État itération : " + count + "  / " + (valCoeff.length *nbIt));



      }

    }

  }

  private static Parameters initialize_parameters(String name) {
    return Parameters.unmarshall(name);
  }
}
