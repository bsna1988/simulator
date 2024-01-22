package ua.edu.krok.taskset.impl;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.util.SupplierUtil;
import ua.edu.krok.scheduler.TaskSet;
import ua.edu.krok.scheduler.impl.DAGProject;
import ua.edu.krok.scheduler.impl.TimedTask;
import ua.edu.krok.taskset.TaskSetGenerator;

public class RandomDAG implements TaskSetGenerator {

    @Override
    public TaskSet<TimedTask> generate(int tasksCount, int edgesCount, int maxEstimatedTime) {
        // Create the VertexFactory so the generator can create vertices
        Supplier<TimedTask> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public TimedTask get() {
                int estimatedTime = Math.max((int) (Math.random() * maxEstimatedTime), 1);
                return new TimedTask(id++, estimatedTime);
            }
        };
        DirectedAcyclicGraph<TimedTask, DefaultEdge> graph =
            new DirectedAcyclicGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        Map<Integer, TimedTask> tasksMap = new HashMap<>();
        for (int i = 0; i < tasksCount; i++) {
            TimedTask task = graph.addVertex();
            tasksMap.put(task.getId(), task);
        }

        while (graph.edgeSet().size() < edgesCount) {
            TimedTask fromVertex = tasksMap.get((int) (Math.random() * tasksCount));
            TimedTask toVertex = tasksMap.get((int) (Math.random() * tasksCount));

            if ((fromVertex.getId() < toVertex.getId()) &&
                (graph.getAllEdges(fromVertex, toVertex) == null ||
                    graph.getAllEdges(fromVertex, toVertex).isEmpty())) {
                graph.addEdge(fromVertex, toVertex);

            }
        }

        DOTExporter<TimedTask, DefaultEdge> exporter =
            new DOTExporter<>(v -> v.getId() + "");
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label",
                DefaultAttribute.createAttribute("#" + v.getId()
                    + " \n(" + v.getEstimatedHours() + "h)"));
            return map;
        });
        Writer writer = new StringWriter();
        exporter.exportGraph(graph, writer);
       // System.out.println(writer);

        DAGProject<TimedTask> project = new DAGProject<>();
        for (int i = 0; i < tasksCount; i++) {
            TimedTask task = tasksMap.get(i);
            project.addTask(task, graph.getAncestors(task));
        }
        return project;
    }


    public static void main(String[] args) {
        RandomDAG generator = new RandomDAG();
        generator.generate(10, 10, 8);
    }
}
