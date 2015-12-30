import engine.InMemoryDictionaryTest;
import game.VerbosityGameTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ui.ConsoleRunnerTest;


@RunWith(Suite.class)
@SuiteClasses({ConsoleRunnerTest.class, VerbosityGameTest.class, InMemoryDictionaryTest.class})
public class GameTestSuit {

}
