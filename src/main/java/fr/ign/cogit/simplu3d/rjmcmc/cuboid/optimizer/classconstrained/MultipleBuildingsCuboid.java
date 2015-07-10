package fr.ign.cogit.simplu3d.rjmcmc.cuboid.optimizer.classconstrained;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.random.RandomGenerator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.AdapterFactory;
import fr.ign.cogit.simplu3d.model.application.BasicPropertyUnit;
import fr.ign.cogit.simplu3d.model.application.Environnement;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.endTest.StabilityEndTest;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.energy.cuboid2.DifferenceVolumeUnaryEnergy;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.energy.cuboid2.IntersectionVolumeBinaryEnergy;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.energy.cuboid2.VolumeUnaryEnergy;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.impl.Cuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.loader.LoaderCuboid2;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.simple.ParallelCuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.simple.SimpleCuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.sampler.GreenSamplerBlockTemperature;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.transformation.ChangeHeight;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.transformation.ChangeLength;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.transformation.ChangeWidth;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.transformation.MoveCuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.transformation.RotateCuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.transformation.birth.ParallelPolygonTransform;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.transformation.birth.TransformToSurface;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.visitor.CSVendStats;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.visitor.CSVvisitor;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.visitor.CountVisitor;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.visitor.FilmVisitor;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.visitor.ShapefileVisitorCuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.visitor.StatsVisitor;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.visitor.ViewerVisitor;
import fr.ign.mpp.DirectRejectionSampler;
import fr.ign.mpp.DirectSampler;
import fr.ign.mpp.configuration.BirthDeathModification;
import fr.ign.mpp.configuration.GraphConfiguration;
import fr.ign.mpp.kernel.KernelFactory;
import fr.ign.mpp.kernel.ObjectBuilder;
import fr.ign.mpp.kernel.ObjectSampler;
import fr.ign.mpp.kernel.UniformTypeView;
import fr.ign.parameters.Parameters;
import fr.ign.random.Random;
import fr.ign.rjmcmc.acceptance.MetropolisAcceptance;
import fr.ign.rjmcmc.configuration.ConfigurationModificationPredicate;
import fr.ign.rjmcmc.distribution.PoissonDistribution;
import fr.ign.rjmcmc.energy.BinaryEnergy;
import fr.ign.rjmcmc.energy.ConstantEnergy;
import fr.ign.rjmcmc.energy.MinusUnaryEnergy;
import fr.ign.rjmcmc.energy.MultipliesBinaryEnergy;
import fr.ign.rjmcmc.energy.MultipliesUnaryEnergy;
import fr.ign.rjmcmc.energy.PlusUnaryEnergy;
import fr.ign.rjmcmc.energy.UnaryEnergy;
import fr.ign.rjmcmc.kernel.Kernel;
import fr.ign.rjmcmc.kernel.NullView;
import fr.ign.rjmcmc.kernel.Transform;
import fr.ign.rjmcmc.kernel.Variate;
import fr.ign.rjmcmc.sampler.Sampler;
import fr.ign.simulatedannealing.SimulatedAnnealing;
import fr.ign.simulatedannealing.endtest.EndTest;
import fr.ign.simulatedannealing.endtest.MaxIterationEndTest;
import fr.ign.simulatedannealing.schedule.GeometricSchedule;
import fr.ign.simulatedannealing.schedule.Schedule;
import fr.ign.simulatedannealing.temperature.SimpleTemperature;
import fr.ign.simulatedannealing.visitor.CompositeVisitor;
import fr.ign.simulatedannealing.visitor.OutputStreamVisitor;
import fr.ign.simulatedannealing.visitor.Visitor;

/**
 * 
 * This software is released under the licence CeCILL
 * 
 * see LICENSE.TXT
 * 
 * see <http://www.cecill.info/ http://www.cecill.info/
 * 
 * 
 * 
 * @copyright IGN
 * 
 * @author Brasebin Mickaël
 * 
 * @version 1.0
 **/
public class MultipleBuildingsCuboid {

  private double coeffDec = Double.NaN;
  private double deltaConf = Double.NaN;

  private double minLengthBox = Double.NaN;
  private double maxLengthBox = Double.NaN;
  private double minWidthBox = Double.NaN;
  private double maxWidthBox = Double.NaN;

  private double energyCreation = Double.NaN;
  private IGeometry samplingSurface = null;

  public void setSamplingSurface(IGeometry geom) {
    samplingSurface = geom;
  }

  public void setEnergyCreation(double energyCreation) {
    this.energyCreation = energyCreation;
  }

  public void setMinLengthBox(double minLengthBox) {
    this.minLengthBox = minLengthBox;
  }

  public void setMaxLengthBox(double maxLengthBox) {
    this.maxLengthBox = maxLengthBox;
  }

  public void setMinWidthBox(double minWidthBox) {
    this.minWidthBox = minWidthBox;
  }

  public void setMaxWidthBox(double maxWidthBox) {
    this.maxWidthBox = maxWidthBox;
  }

  public MultipleBuildingsCuboid() {
  }

  public void setCoeffDec(double coeffDec) {
    this.coeffDec = coeffDec;
  }

  public void setDeltaConf(double deltaConf) {
    this.deltaConf = deltaConf;
  }

  public GraphConfiguration<Cuboid> process(BasicPropertyUnit bpu, Parameters p, Environnement env, int id,
      ConfigurationModificationPredicate<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> pred) throws Exception {
    // Géométrie de l'unité foncière sur laquelle porte la génération
    IGeometry geom = bpu.generateGeom().buffer(1);

    // Définition de la fonction d'optimisation (on optimise en décroissant)
    // relative au volume
    GraphConfiguration<Cuboid> conf = null;

    try {
      conf = create_configuration(p, geom, bpu);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // Création de l'échantilloneur
    Sampler<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> samp = create_sampler(Random.random(), p, bpu,
        pred);
    // Température
    Schedule<SimpleTemperature> sch = create_schedule(p);

    int loadExistingConfig = p.getInteger("load_existing_config");
    if (loadExistingConfig == 1) {
      String configPath = p.get("config_shape_file").toString();
      List<Cuboid> lCuboid = LoaderCuboid2.loadFromShapeFile(configPath);
      BirthDeathModification<Cuboid> m = conf.newModification();
      for (Cuboid c : lCuboid) {
        m.insertBirth(c);
      }
      conf.deltaEnergy(m);
      // conf.apply(m);
      m.apply(conf);
      System.out.println("First update OK");
    }
    // EndTest<Cuboid2, Configuration<Cuboid2>, SimpleTemperature,
    // Sampler<Cuboid2, Configuration<Cuboid2>, SimpleTemperature>> end =
    // create_end_test(p);

    EndTest end = null;
    if (p.getBoolean(("isAbsoluteNumber"))) {
      end = create_end_test(p);
    } else {
      end = create_end_test_stability(p);
    }

    List<Visitor<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>>> list = new ArrayList<>();
    if (p.getBoolean("outputstreamvisitor")) {
      Visitor<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> visitor = new OutputStreamVisitor<>(
          System.out);
      list.add(visitor);
    }
    if (p.getBoolean("shapefilewriter")) {
      Visitor<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> shpVisitor = new ShapefileVisitorCuboid<>(p
          .get("result").toString() + "result");
      list.add(shpVisitor);
    }
    if (p.getBoolean("visitorviewer")) {
      ViewerVisitor<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> visitorViewer = new ViewerVisitor<>(
          "" + id, p);
      list.add(visitorViewer);
    }

    if (p.getBoolean("statsvisitor")) {
      StatsVisitor<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> statsViewer = new StatsVisitor<>(
          "Énergie");
      list.add(statsViewer);
    }

    if (p.getBoolean("filmvisitor")) {
      IDirectPosition dpCentre = new DirectPosition(p.getDouble("filmvisitorx"), p.getDouble("filmvisitory"),
          p.getDouble("filmvisitorz"));
      Vecteur viewTo = new Vecteur(p.getDouble("filmvisitorvectx"), p.getDouble("filmvisitorvecty"),
          p.getDouble("filmvisitorvectz"));
      Color c = new Color(p.getInteger("filmvisitorr"), p.getInteger("filmvisitorg"), p.getInteger("filmvisitorb"));
      FilmVisitor<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> visitorViewerFilmVisitor = new FilmVisitor<>(
          dpCentre, viewTo, p.getString("result"), c, p);
      list.add(visitorViewerFilmVisitor);
    }

    if (p.getBoolean("csvvisitorend")) {
      String fileName = p.get("result").toString() + p.get("csvfilenamend");
      CSVendStats<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> statsViewer = new CSVendStats<>(
          fileName);
      list.add(statsViewer);
    }
    if (p.getBoolean("csvvisitor")) {
      String fileName = p.get("result").toString() + p.get("csvfilename");
      CSVvisitor<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> statsViewer = new CSVvisitor<>(
          fileName);
      list.add(statsViewer);
    }
    countV = new CountVisitor<>();
    list.add(countV);
    CompositeVisitor<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> mVisitor = new CompositeVisitor<>(list);
    init_visitor(p, mVisitor);
    /*
     * < This is the way to launch the optimization process. Here, the magic
     * happen... >
     */
    SimulatedAnnealing.optimize(Random.random(), conf, samp, sch, end, mVisitor);
    return conf;
  }

  // Initialisation des visiteurs
  // nbdump => affichage dans la console
  // nbsave => sauvegarde en shapefile
  static void init_visitor(Parameters p, Visitor<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> v) {
    v.init(p.getInteger("nbdump"), p.getInteger("nbsave"));
  }

  CountVisitor<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> countV = null;

  public int getCount() {
    return countV.getCount();
  }

  public GraphConfiguration<Cuboid> create_configuration(Parameters p, IGeometry geom, BasicPropertyUnit bpu)
      throws Exception {

    return this.create_configuration(p, AdapterFactory.toGeometry(new GeometryFactory(), geom), bpu);

  }

  // Création de la configuration
  /**
   * @param p
   *          paramètres importés depuis le fichier XML
   * @param bpu
   *          l'unité foncière considérée
   * @return la configuration chargée, c'est à dire la formulation énergétique
   *         prise en compte
   */
  public GraphConfiguration<Cuboid> create_configuration(Parameters p, Geometry geom, BasicPropertyUnit bpu) {
    // Énergie constante : à la création d'un nouvel objet

    double energyCrea = Double.isNaN(this.energyCreation) ? p.getDouble("energy") : this.energyCreation;

    ConstantEnergy<Cuboid, Cuboid> energyCreation = new ConstantEnergy<Cuboid, Cuboid>(energyCrea);

    // Énergie constante : pondération de l'intersection
    ConstantEnergy<Cuboid, Cuboid> ponderationVolume = new ConstantEnergy<Cuboid, Cuboid>(
        p.getDouble("ponderation_volume"));
    // Énergie unaire : aire dans la parcelle
    UnaryEnergy<Cuboid> energyVolume = new VolumeUnaryEnergy<Cuboid>();
    // Multiplication de l'énergie d'intersection et de l'aire
    UnaryEnergy<Cuboid> energyVolumePondere = new MultipliesUnaryEnergy<Cuboid>(ponderationVolume, energyVolume);

    // On retire de l'énergie de création, l'énergie de l'aire
    UnaryEnergy<Cuboid> u3 = new MinusUnaryEnergy<Cuboid>(energyCreation, energyVolumePondere);

    // Énergie constante : pondération de la différence
    ConstantEnergy<Cuboid, Cuboid> ponderationDifference = new ConstantEnergy<Cuboid, Cuboid>(
        p.getDouble("ponderation_difference_ext"));
    // On ajoute l'énergie de différence : la zone en dehors de la parcelle
    UnaryEnergy<Cuboid> u4 = new DifferenceVolumeUnaryEnergy<Cuboid>(geom);
    UnaryEnergy<Cuboid> u5 = new MultipliesUnaryEnergy<Cuboid>(ponderationDifference, u4);
    UnaryEnergy<Cuboid> unaryEnergy = new PlusUnaryEnergy<Cuboid>(u3, u5);

    // Énergie binaire : intersection entre deux rectangles
    ConstantEnergy<Cuboid, Cuboid> c3 = new ConstantEnergy<Cuboid, Cuboid>(p.getDouble("ponderation_volume_inter"));
    BinaryEnergy<Cuboid, Cuboid> b1 = new IntersectionVolumeBinaryEnergy<Cuboid>();
    BinaryEnergy<Cuboid, Cuboid> binaryEnergy = new MultipliesBinaryEnergy<Cuboid, Cuboid>(c3, b1);
    // empty initial configuration*/
    GraphConfiguration<Cuboid> conf = new GraphConfiguration<>(unaryEnergy, binaryEnergy);
    return conf;
  }

  /**
   * Sampler
   * 
   * @param p
   *          les paramètres chargés depuis le fichier xml
   * @param r
   *          l'enveloppe dans laquelle on génère les positions
   * @return
   * @throws Exception 
   */
  public Sampler<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> create_sampler(RandomGenerator rng,
      Parameters p, BasicPropertyUnit bpU,
      ConfigurationModificationPredicate<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> pred) throws Exception {
    // Un vecteur ?????
    double minlen = Double.isNaN(this.minLengthBox) ? p.getDouble("minlen") : this.minLengthBox;
    double maxlen = Double.isNaN(this.maxLengthBox) ? p.getDouble("maxlen") : this.maxLengthBox;

    double minwid = Double.isNaN(this.minWidthBox) ? p.getDouble("minwid") : this.minWidthBox;
    double maxwid = Double.isNaN(this.maxWidthBox) ? p.getDouble("maxwid") : this.maxWidthBox;

    double minheight = p.getDouble("minheight");
    double maxheight = p.getDouble("maxheight");

    IEnvelope env = bpU.getGeom().envelope();
    if (samplingSurface == null) {
      samplingSurface = bpU.getpol2D();
    }
    // in multi object situations, we need an object builder for each subtype
    // and a sampler for the supertype (end of file)

    Vector<Double> v = new Vector<>();
    v.add(env.minX());
    v.add(env.minY());
    v.add(minlen);
    v.add(minwid);
    v.add(minheight);
    v.add(0.);
    Vector<Double> d = new Vector<>();
    d.add(env.maxX());
    d.add(env.maxY());
    d.add(maxlen);
    d.add(maxwid);
    d.add(maxheight);
    d.add(Math.PI);
    for (int i = 0; i < d.size(); i++) {
      d.set(i, d.get(i) - v.get(i));
    }

    // TODO we should give the proper sampling surface and the limits
    Transform transformParallel = new ParallelPolygonTransform(d, v, samplingSurface, null);
    Transform transformSimple = new TransformToSurface(d, v, samplingSurface);

    // When direct sampling (solomon, etc.), what is the prob to choose a simple
    // cuboid
    double p_simple = 0.5;
    CuboidSampler objectSampler = new CuboidSampler(rng, p_simple, transformSimple, transformParallel);

    // Distribution de poisson
    PoissonDistribution distribution = new PoissonDistribution(rng, p.getDouble("poisson"));

    DirectSampler<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> ds = new DirectRejectionSampler<>(
        distribution, objectSampler, pred);

    // Probabilité de naissance-morts modifications
    List<Kernel<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>>> kernels = new ArrayList<>(3);
    KernelFactory<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> factory = new KernelFactory<>();

    // we also need one birthdeath kernel per object subtype
    Kernel<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> kernel1 = new Kernel<>(
        new NullView<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>>(),
        new UniformTypeView<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>>(SimpleCuboid.class,
            simplebuilder), new Variate(rng), new Variate(rng), transformSimple, p.getDouble("pbirthdeath"),
        p.getDouble("pbirth"));
    kernel1.setName("BirthDeathSimple");
    kernels.add(kernel1);

    Kernel<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> kernel2 = new Kernel<>(
        new NullView<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>>(),
        new UniformTypeView<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>>(SimpleCuboid.class,
            parallelbuilder), new Variate(rng), new Variate(rng), transformParallel, p.getDouble("pbirthdeath"),
        p.getDouble("pbirth"));
    kernel2.setName("BirthDeathParallel");
    kernels.add(kernel2);

    // each move should either work on the super type or on the subtype and use a uniformtypeview
    double amplitudeMove = p.getDouble("amplitudeMove");
    kernels.add(factory
        .make_uniform_modification_kernel(rng, simplebuilder, new MoveCuboid(amplitudeMove), 0.2, "Move"));
    double amplitudeRotate = p.getDouble("amplitudeRotate") * Math.PI / 180;
    kernels.add(factory.make_uniform_modification_kernel(rng, simplebuilder, new RotateCuboid(amplitudeRotate), 0.2,
        "Rotate"));
    double amplitudeMaxDim = p.getDouble("amplitudeMaxDim");
    kernels.add(factory.make_uniform_modification_kernel(rng, simplebuilder, new ChangeWidth(amplitudeMaxDim), 0.2,
        "ChgWidth"));
    kernels.add(factory.make_uniform_modification_kernel(rng, simplebuilder, new ChangeLength(amplitudeMaxDim), 0.2,
        "ChgLength"));
    double amplitudeHeight = p.getDouble("amplitudeHeight");
    kernels.add(factory.make_uniform_modification_kernel(rng, simplebuilder, new ChangeHeight(amplitudeHeight), 0.2,
        "ChgHeight"));

    Sampler<GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> s = new GreenSamplerBlockTemperature<>(ds,
        new MetropolisAcceptance<SimpleTemperature>(), kernels);
    return s;
  }

  private static EndTest create_end_test(Parameters p) {
    return new MaxIterationEndTest(p.getInteger("nbiter"));
  }

  private EndTest create_end_test_stability(Parameters p) {
    double loc_deltaconf;
    if (Double.isNaN(this.deltaConf)) {
      loc_deltaconf = p.getDouble("delta");
    } else {
      loc_deltaconf = this.deltaConf;
    }
    return new StabilityEndTest<Cuboid>(p.getInteger("nbiter"), loc_deltaconf);
  }

  private Schedule<SimpleTemperature> create_schedule(Parameters p) {
    double coefDef = 0;
    if (Double.isNaN(this.coeffDec)) {
      coefDef = p.getDouble("deccoef");
    } else {
      coefDef = this.coeffDec;
    }
    return new GeometricSchedule<SimpleTemperature>(new SimpleTemperature(p.getDouble("temp")), coefDef);
  }

  static ObjectBuilder<Cuboid> simplebuilder = new ObjectBuilder<Cuboid>() {
    @Override
    public Cuboid build(Vector<Double> coordinates) {
      return new SimpleCuboid(coordinates.get(0), coordinates.get(1), coordinates.get(2), coordinates.get(3),
          coordinates.get(4), coordinates.get(5));
    }

    @Override
    public int size() {
      return 6;
    }

    @Override
    public void setCoordinates(Cuboid t, List<Double> coordinates) {
      SimpleCuboid sc = (SimpleCuboid) t;
      coordinates.set(0, sc.centerx);
      coordinates.set(1, sc.centery);
      coordinates.set(2, sc.length);
      coordinates.set(3, sc.width);
      coordinates.set(4, sc.height);
      coordinates.set(5, sc.orientation);
    }
  };

  static ObjectBuilder<Cuboid> parallelbuilder = new ObjectBuilder<Cuboid>() {
    @Override
    public Cuboid build(Vector<Double> coordinates) {
      return new ParallelCuboid(coordinates.get(0), coordinates.get(1), coordinates.get(2), coordinates.get(3),
          coordinates.get(4), coordinates.get(5));
    }

    @Override
    public int size() {
      return 6;
    }

    @Override
    public void setCoordinates(Cuboid t, List<Double> coordinates) {
      ParallelCuboid pc = (ParallelCuboid) t;
      coordinates.set(0, pc.centerx);
      coordinates.set(1, pc.centery);
      coordinates.set(2, pc.length);
      coordinates.set(3, pc.width);
      coordinates.set(4, pc.height);
      coordinates.set(5, pc.orientation);
    }
  };

  public static class CuboidSampler implements ObjectSampler<Cuboid> {
    RandomGenerator engine;
    double p_simple;
    Cuboid object;
    Variate variate;
    Transform transformSimple;
    Transform transformParallel;

    public CuboidSampler(RandomGenerator e, double p_simple, Transform transformSimple, Transform transformParallel) {
      this.engine = e;
      this.p_simple = p_simple;
      this.transformSimple = transformSimple;
      this.transformParallel = transformParallel;
      this.variate = new Variate(e);
    }

    @Override
    public double sample(RandomGenerator e) {
      Vector<Double> var0 = new Vector<>();
      Vector<Double> val1 = new Vector<>();
      if (engine.nextDouble() < p_simple) {
        var0.setSize(6);
        val1.setSize(6);
        double phi = this.variate.compute(var0);
        double jacob = this.transformSimple.apply(true, new Vector<Double>(0), var0, val1, new Vector<Double>(0));
        this.object = simplebuilder.build(val1);
        return phi / jacob;
      }
      var0.setSize(6);
      val1.setSize(6);
      double phi = this.variate.compute(var0);
      double jacob = this.transformParallel.apply(true, new Vector<Double>(0), var0, val1, new Vector<Double>(0));
      this.object = parallelbuilder.build(val1);
      return phi / jacob;
    }

    @Override
    public double pdf(Cuboid t) {
      if (SimpleCuboid.class.isInstance(t)) {
        Vector<Double> val1 = new Vector<>();
        val1.setSize(6);
        simplebuilder.setCoordinates(t, val1);
        Vector<Double> val0 = new Vector<>();
        val0.setSize(6);
        double J10 = this.transformSimple.apply(false, val1, new Vector<Double>(0), new Vector<Double>(0), val0);
        double pdf = this.variate.pdf(val0);
        return pdf * J10;
      }
      Vector<Double> val1 = new Vector<>();
      val1.setSize(6);
      parallelbuilder.setCoordinates(t, val1);
      Vector<Double> val0 = new Vector<>();
      val0.setSize(6);
      double J10 = this.transformParallel.apply(false, val1, new Vector<Double>(0), new Vector<Double>(0), val0);
      double pdf = this.variate.pdf(val0);
      return pdf * J10;
    }

    @Override
    public Cuboid getObject() {
      return this.object;
    }
  }
}