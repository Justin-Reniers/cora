/**************************************************************************************************
 Copyright 2019 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

package cora;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.CoraInputReader;
import cora.parsers.LcTrsInputReader;
import cora.parsers.TrsInputReader;
import cora.provingstrategies.LocalConfluence;
import cora.provingstrategies.LocalConfluenceExtended;
import cora.provingstrategies.Orthogonality;
import cora.provingstrategies.StrategyInherit;
import cora.smt.EquivalenceProof;
import hci.InputPresenter;
import hci.InputModel;
import hci.InputView;

import java.util.ArrayList;
import java.util.List;

class CliArgs {
  /**
   * Class that creates commandline argument specifics like flags
   */

  /**
   * List of all parameters.
   */
  @Parameter
  private List<String> parameters = new ArrayList<>();

  /**
   * Parameter for the path of the input file of a TRS
   */
  @Parameter(names = { "-i", "--input", "--lctrs"}, description = "Input file", required = true)
  String inputFilePath;

  /**
   * Parameter for the technique or strategy to be applied to the TRS
   */
  @Parameter(names = {"-t", "--technique"}, description = "Strategy used")
  String strategy = "orthogonal";

  /**
   * Maximum timeout in seconds before the program stops trying given strategy on given TRS
   */
  @Parameter(names = {"--timeout"}, description = "Timeout in seconds")
  int timeout = 5;

  /**
   * Flag telling whether TRS is terminating or not
   */
  @Parameter(names = {"--terminating"}, description = "All systems are terminating")
  boolean terminating = false;
}

public class Main {
  private static String getExtension(String filename) {
    int i = filename.lastIndexOf('.');
    if (i >= 0) return filename.substring(i+1);
    return "";
  }

  private static TRS readInput(String file) throws Exception {
    String extension = getExtension(file);
    if (extension.equals("trs") || extension.equals("mstrs")) {
      return TrsInputReader.readTrsFromFile(file);
    }
    if (extension.equals("cora")) {
      return CoraInputReader.readProgramFromFile(file);
    }
    if (extension.equals("lctrs")) {
      return LcTrsInputReader.readLcTrsFromFile(file);
    }
    throw new Exception("Unknown file extension: " + extension + ".");
  }

  private static StrategyInherit getStrategy(CliArgs args) throws Exception {
    TRS trs = readInput(args.inputFilePath);
    switch (args.strategy) {
      case "orthogonal":
        return new Orthogonality(trs);
      case "lc":
        return new LocalConfluence(trs, args.terminating);
      case "lce":
        return new LocalConfluenceExtended(trs, args.terminating);
      default:
        throw new Exception("Unknown strategy: " + args.strategy);
    }
  }

  /**
   *
   * @param args
   */
  public static void main(String[] args) {
    try {
      if (args.length == 0) args = new String[] {"-i", "src/test/java/utils/recursive_fact.lctrs", "-t", "lc"};

      CliArgs cliArgs = new CliArgs();
      JCommander.newBuilder().addObject(cliArgs).build().parse(args);

      new Logger(new ConsoleLogger());
      /*
      StrategyInherit strat = getStrategy(cliArgs);
      Result result = strat.apply(cliArgs.timeout);
      Logger.log("Result type: " + result.getResult());
      Logger.log("Time taken: " + result.getTime() + "ms");
      Logger.finalized();

      System.out.println("Try just method");
      LocalConfluence lc = new LocalConfluence(trs, false);
      Result res = lc.apply();
      System.out.println(res.getResult());
      System.exit(0);
      */

    } catch (Exception e) {
      e.printStackTrace();
    }

    InputPresenter ic = new InputPresenter(new InputView("LcTrs equivalence proof assistant"), new InputModel());
    ic.run();
  }
}
