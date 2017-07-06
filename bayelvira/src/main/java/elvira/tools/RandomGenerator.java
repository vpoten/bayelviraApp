package elvira.tools;
import java.util.GregorianCalendar;
import java.util.Random;

/**
     * This class deal with the generation of random numbers. Maintainig 
     * the same <code>Random</code> object we avoid to repeat the same seed when
     * two AuxiliarPotentialTables are instantiated in the same milisecond
     */
    public class RandomGenerator
    {
      /**
       * Random numbers generator 
       */
      static private Random randomGenerator;
      /**
       * Instance of the class
       */
      static private RandomGenerator _instance = null;

      /**
       * Basic constructor
       */
      private RandomGenerator()
      {
        randomGenerator = new Random();        
      }

      /**
       * Basic constructor
       * 
       * @param seed seed to initialize the random numbers generator
       */
      private RandomGenerator(long seed)
      {
        randomGenerator = new Random(seed);        
      }
      
      /**
       * Returns a <code>Random</code> object to generate random numbers. This object
       * is the same all the time, it does not matter the instance of AuxiliarPotentialTable
       * where we call this method from.
       * 
       * @return A Random object
       */
      public static Random getRandomGenerator()
      { 
        if(null == _instance)
        {
          _instance = new RandomGenerator();
        }
        return _instance.randomGenerator;        
      }

      /**
       * Sets the seed of the <code>Random</code> object to <code>seed</code>
       */
      public static void setRandomSeed(long seed)
      {
        _instance = new RandomGenerator(seed);
      }

    }