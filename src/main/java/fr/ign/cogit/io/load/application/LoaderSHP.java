package fr.ign.cogit.io.load.application;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTM;
import fr.ign.cogit.geoxygene.sig3d.util.ColorShade;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.importer.AssignLinkToBordure;
import fr.ign.cogit.importer.BordureImporter;
import fr.ign.cogit.importer.BuildingImporter;
import fr.ign.cogit.importer.SousParcelleImporter;
import fr.ign.cogit.importer.VoirieImporter;
import fr.ign.cogit.model.application.Batiment;
import fr.ign.cogit.model.application.Environnement;
import fr.ign.cogit.model.application.Parcelle;
import fr.ign.cogit.model.application.SousParcelle;
import fr.ign.cogit.model.application.Voirie;
import fr.ign.cogit.model.application.Zone;
import fr.ign.cogit.sig3d.convert.geom.FromGeomToSurface;
import fr.ign.cogit.util.AssignZ;

public class LoaderSHP {

  /*
   * Nom des fichiers en entrée
   */
  public final static String NOM_FICHIER_ZONAGE = "zonage.shp";
  public final static String NOM_FICHIER_PARCELLE = "parcelle.shp";
  public final static String NOM_FICHIER_VOIRIE = "route.shp";
  public final static String NOM_FICHIER_BATIMENTS = "bati.shp";
  public final static String NOM_FICHIER_TERRAIN = "MNT_BD3D.asc";
  
  
  
  
  public final static boolean SURSAMPLED = true;

  /*
   * Attributs du fichier zone
   */
  public final static String NOM_ATT_TYPE_ZONE = "TYPEZONE";

  public static Environnement load(String folder) {
    Environnement env = new Environnement();

    // Chargement des fichiers

    IFeatureCollection<IFeature> zoneColl = ShapefileReader.read(folder
        + NOM_FICHIER_ZONAGE);
    IFeatureCollection<IFeature> parcelleColl = ShapefileReader.read(folder
        + NOM_FICHIER_PARCELLE);
    IFeatureCollection<IFeature> voirieColl = ShapefileReader.read(folder
        + NOM_FICHIER_VOIRIE);
    IFeatureCollection<IFeature> batiColl = ShapefileReader.read(folder
        + NOM_FICHIER_BATIMENTS);
    
    
    //Etape 0 : doit on translater tous les objets ?
    
    if(Environnement.TRANSLATE_TO_ZERO){
      
      Environnement.dpTranslate = zoneColl.envelope().center();
      
      
      for(IFeature feat: zoneColl){
        feat.setGeom(feat.getGeom().translate(- Environnement.dpTranslate.getX(),-  Environnement.dpTranslate.getY(), 0));
        
      }
      
      
      for(IFeature feat: parcelleColl){
        feat.setGeom(feat.getGeom().translate(- Environnement.dpTranslate.getX(),-  Environnement.dpTranslate.getY(), 0));
        
      }
      
      for(IFeature feat: voirieColl){
        feat.setGeom(feat.getGeom().translate(- Environnement.dpTranslate.getX(),-  Environnement.dpTranslate.getY(), 0));
        
      }
      
      for(IFeature feat: batiColl){
        feat.setGeom(feat.getGeom().translate(- Environnement.dpTranslate.getX(),-  Environnement.dpTranslate.getY(), 0));
        
      }
      
      
      
      
    }
    
    
    

    // Etape 1 : chargement des parcelles et créations des bordures
//    IFeatureCollection<Parcelle> parcelles = BordureImporter
//        .assignBordureToParcelle(parcelleColl);
    IFeatureCollection<Parcelle> parcelles = BordureImporter
    .assignBordureToParcelleWithOrientation(parcelleColl, 1.5);
    

    env.setParcelles(parcelles);

    // Etape 2 : création des zones
    IFeatureCollection<Zone> zones = new FT_FeatureCollection<Zone>();

    for (IFeature feat : zoneColl) {
      Zone z = new Zone(FromGeomToSurface.convertMSGeom(feat.getGeom()));

      z.setType(feat.getAttribute(NOM_ATT_TYPE_ZONE).toString());

      zones.add(z);

    }

    env.setZones(zones);

    // Etape 3 : création des sous parcelles

    IFeatureCollection<SousParcelle> sousParcelles = SousParcelleImporter
        .create(parcelles, zones);
    env.setSousParcelles(sousParcelles);

    // Etape 4 : import des bâtiments
    IFeatureCollection<Batiment> batiments = BuildingImporter.importBuilding(
        batiColl, sousParcelles);
    env.setBatiments(batiments);

    // Etape 5 : chargement des rues
    IFeatureCollection<Voirie> voiries = VoirieImporter
        .importVoirie(voirieColl);
    env.setVoiries(voiries);
    

    //Etape 6 : on affecte les liens entres une bordure et ses objets adjacents
    AssignLinkToBordure.process(sousParcelles, voiries);
    
    
    //Etape 7 : on affecte des z à tout ce bon monde
    // - parcelles, sous-parcelles route sans z, zonage, les bordures etc...
    DTM dtm  = new DTM(folder+NOM_FICHIER_TERRAIN, "Terrain", true, 1, ColorShade.BLUE_CYAN_GREEN_YELLOW_WHITE);
    try {
      AssignZ.toParcelle(env.getParcelles(), dtm, SURSAMPLED);
      AssignZ.toSousParcelle(env.getSousParcelles(), dtm, SURSAMPLED);
      AssignZ.toZone(env.getZones(), dtm, false);
      AssignZ.toVoirie(env.getVoiries(), dtm, SURSAMPLED);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    


    return env;
  }

}
