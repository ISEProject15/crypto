package project.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import project.lib.crypto.algorithm.*;
import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;
import project.lib.scaffolding.collections.SequenceFunnel;
import project.lib.scaffolding.streaming.StreamBuffer;
import project.scaffolding.IntMath;
import project.scaffolding.debug.BinaryDebug;
import project.scaffolding.debug.IndentedAppendable;
import project.test.attacks.CommonModulusAttack;
import project.test.attacks.CommonModulusAttackMaliciousUser;
import project.test.attacks.FactorizeAttack;
import project.test.scaffolding.DoubleRandomAccess;
import project.test.scaffolding.drawing.Graph;
import project.test.scaffolding.statistics.Statistic;
import project.test.scaffolding.statistics.StatisticSummary;
import project.test.scaffolding.testing.TestCollector;
import project.test.scaffolding.testing.TestExecutor;
import project.test.scaffolding.testing.TestExecutorOptions;

public class App {
    public static void main(String[] args) throws Exception {
        CommonModulusAttackMaliciousUser.demo();

    }
}