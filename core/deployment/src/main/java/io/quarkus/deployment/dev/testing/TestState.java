package io.quarkus.deployment.dev.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;

public class TestState {

    final Map<String, Map<UniqueId, TestResult>> resultsByClass = new HashMap<>();
    final Set<UniqueId> failing = new HashSet<>();

    public List<String> getClassNames() {
        return new ArrayList<>(resultsByClass.keySet()).stream().sorted().collect(Collectors.toList());
    }

    public List<TestClassResult> getPassingClasses() {
        List<TestClassResult> ret = new ArrayList<>();
        for (Map.Entry<String, Map<UniqueId, TestResult>> i : resultsByClass.entrySet()) {
            List<TestResult> passing = new ArrayList<>();
            List<TestResult> failing = new ArrayList<>();
            List<TestResult> skipped = new ArrayList<>();
            for (TestResult j : i.getValue().values()) {
                if (j.getTestExecutionResult().getStatus() == TestExecutionResult.Status.FAILED) {
                    failing.add(j);
                } else if (j.getTestExecutionResult().getStatus() == TestExecutionResult.Status.ABORTED) {
                    skipped.add(j);
                } else {
                    passing.add(j);
                }
            }
            if (failing.isEmpty()) {
                TestClassResult p = new TestClassResult(i.getKey(), passing, failing, skipped);
                ret.add(p);
            }
        }

        Collections.sort(ret);
        return ret;
    }

    public List<TestClassResult> getFailingClasses() {
        List<TestClassResult> ret = new ArrayList<>();
        for (Map.Entry<String, Map<UniqueId, TestResult>> i : resultsByClass.entrySet()) {
            List<TestResult> passing = new ArrayList<>();
            List<TestResult> failing = new ArrayList<>();
            List<TestResult> skipped = new ArrayList<>();
            for (TestResult j : i.getValue().values()) {
                if (j.getTestExecutionResult().getStatus() == TestExecutionResult.Status.FAILED) {
                    failing.add(j);
                } else if (j.getTestExecutionResult().getStatus() == TestExecutionResult.Status.ABORTED) {
                    skipped.add(j);
                } else {
                    passing.add(j);
                }
            }
            if (!failing.isEmpty()) {
                TestClassResult p = new TestClassResult(i.getKey(), passing, failing, skipped);
                ret.add(p);
            }
        }
        Collections.sort(ret);
        return ret;
    }

    public synchronized void updateResults(Map<String, Map<UniqueId, TestResult>> latest) {
        for (Map.Entry<String, Map<UniqueId, TestResult>> entry : latest.entrySet()) {
            Map<UniqueId, TestResult> existing = this.resultsByClass.get(entry.getKey());
            if (existing == null) {
                resultsByClass.put(entry.getKey(), entry.getValue());
            } else {
                existing.putAll(entry.getValue());
            }
            for (Map.Entry<UniqueId, TestResult> r : entry.getValue().entrySet()) {
                if (r.getValue().getTestExecutionResult().getStatus() == TestExecutionResult.Status.FAILED) {
                    failing.add(r.getKey());
                } else {
                    failing.remove(r.getKey());
                }
            }
        }
    }

    public synchronized void classesRemoved(Set<String> classNames) {
        for (String i : classNames) {
            resultsByClass.remove(i);
        }
    }

    public Map<String, Map<UniqueId, TestResult>> getCurrentResults() {
        return Collections.unmodifiableMap(resultsByClass);
    }

    public int getTotalFailures() {
        int count = 0;
        for (Map<UniqueId, TestResult> i : resultsByClass.values()) {
            for (TestResult j : i.values()) {
                if (j.getTestExecutionResult().getStatus() == TestExecutionResult.Status.FAILED) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<TestResult> getHistoricFailures(Map<String, Map<UniqueId, TestResult>> currentResults) {
        List<TestResult> ret = new ArrayList<>();
        for (Map.Entry<String, Map<UniqueId, TestResult>> entry : resultsByClass.entrySet()) {
            for (TestResult j : entry.getValue().values()) {
                if (j.getTestExecutionResult().getStatus() == TestExecutionResult.Status.FAILED) {
                    if (currentResults.containsKey(entry.getKey())) {
                        if (currentResults.get(entry.getKey()).containsKey(j.getUniqueId())) {
                            continue;
                        }
                    }
                    ret.add(j);
                }
            }
        }
        return ret;
    }

    public boolean isFailed(TestDescriptor testDescriptor) {
        return failing.contains(testDescriptor.getUniqueId());
    }
}
