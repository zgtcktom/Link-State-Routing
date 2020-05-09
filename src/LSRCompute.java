import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LSRCompute {
    public static class Network {
        public static class Node {
            private final String name;
            private final List<Pair<Node, Integer>> links;

            // check if a node is in the links
            public boolean has(Node node) {
                for (Pair<Node, Integer> link : links)
                    if (link.getKey() == node) return true;
                return false;
            }

            // add a node to the links
            public void connect(Node node, Integer cost) {
                if (!has(node)) {
                    links.add(new Pair<>(node, cost));
                    links.sort(Comparator.comparing(a -> a.getKey().getName()));
                }
            }

            // remove a node from the links
            public void disconnect(Node node) {
                links.removeIf(link -> link.getKey() == node);
            }

            // constructor
            public Node(String name) {
                this.name = name;
                this.links = new ArrayList<>();
            }

            public String getName() {
                return name;
            }

            public List<Pair<Node, Integer>> getLinks() {
                return links;
            }

            @Override
            public String toString() {
                List<String> list = new ArrayList<>();
                for (Pair<Node, Integer> link : links) {
                    list.add(link.getKey().getName() + ':' + link.getValue());
                }
                return name + ": " + String.join(" ", list);
            }
        }

        private List<Node> nodes;

        // break link between 2 nodes (bidirectional)
        public void breakLink(String a, String b) {
            if (has(a) && has(b)) {
                get(a).disconnect(get(b));
                get(b).disconnect(get(a));
            }
        }

        // remove the node and break link all the links (bidirectional)
        public void remove(String name) {
            if (has(name)) {
                for (Node node : nodes) {
                    node.disconnect(get(name));
                }
            }
            nodes.removeIf(node -> node.getName().equals(name));
        }

        // check if a node is in the network
        public boolean has(String name) {
            for (Node node : nodes)
                if (node.getName().equals(name)) return true;
            return false;
        }

        // get node by name
        public Node get(String name) {
            for (Node node : nodes)
                if (node.getName().equals(name)) return node;
            return null;
        }

        // get node by index
        public Node get(Integer i) {
            return nodes.get(i);
        }

        // get all names
        public List<String> getNames() {
            List<String> names = new ArrayList<>();
            for (Node node : nodes) {
                names.add(node.getName());
            }
            return names;
        }

        // add node by name (or return the existing node) and return the new node
        public Node add(String name) {
            if (has(name)) {
                return get(name);
            }
            Node node = new Node(name);
            nodes.add(node);
            nodes.sort(Comparator.comparing(Node::getName));
            return node;
        }

        public Integer indexOf(Node node) {
            return nodes.indexOf(node);
        }

        public Network() {
            init();
        }

        @Override
        public String toString() {
            List<String> list = new ArrayList<>();
            for (Node node : nodes) {
                list.add(node.toString());
            }
            return String.join("\n", list);
        }

        // load from single line "H: F:9 E:2"
        public void load(String data) {
            String[] links = data.split(" ");
            Node node = add(links[0].substring(0, links[0].length() - 1));
            for (int i = 1; i < links.length; i++) {
                String[] params = links[i].split(":");
                Node neighbor = add(params[0]);
                Integer cost = Integer.parseInt(params[1]);
                node.connect(neighbor, cost);
                neighbor.connect(node, cost);
            }
        }

        public void init() {
            nodes = new ArrayList<>();
        }
    }

    public Network network;

    // load from file
    public void load(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            network.load(line);
        }
    }

    boolean useGUI = false;

    public void dijkstra(String src, boolean waitKey) {
        List<String> names = network.getNames();

        int n = names.indexOf(src);
        int N = names.size();

        int[] dist = new int[N];
        Boolean[] visited = new Boolean[N];
        List<String>[] path = new ArrayList[N];

        for (int i = 0; i < N; i++) {
            dist[i] = Integer.MAX_VALUE;
            visited[i] = false;
        }

        dist[n] = 0;
        path[n] = new ArrayList<>();
        path[n].add(src);

        if (waitKey) log("Steps");

        for (int i = 0; i < N; i++) { // worst case would be to visit all nodes, thus bounded by N
            int minDist = Integer.MAX_VALUE, minInd = -1;

            for (int j = 0; j < N; j++) { // select src initially because dist[n] = 0, select node that current has min distance from src
                if (!visited[j] && dist[j] < minDist) {
                    minDist = dist[j];
                    minInd = j;
                }
            }

            if (minInd != -1) {

                visited[minInd] = true;

                for (Pair<Network.Node, Integer> pair : network.get(minInd).getLinks()) { // get all the neighbor nodes of current node
                    Network.Node node = pair.getKey();
                    Integer cost = pair.getValue();

                    int k = network.indexOf(node);
                    if (dist[minInd] + cost < dist[k]) { // check if visited, or check if current route is the shortest route yet (dist[k] is total cost of src -> k)
                        String name = pair.getKey().getName();
                        dist[k] = dist[minInd] + cost;

                        path[k] = new ArrayList<>();
                        path[k].addAll(path[minInd]);
                        path[k].add(name);

                        if (waitKey) {
                            log("Found " + name + ": Path: " + String.join(">", path[k]) + " Cost: " + dist[k], "");
                            if (useGUI) {
                                log("");
                            } else {
                                log(" [press any key to continue]", "");
                                new Scanner(System.in).nextLine();
                            }
                        }
                    }
                }
            }
        }

        if (waitKey) log("\nSummary table");
        log("Source " + src);
        for (int i = 0; i < N; i++) {
            if (i == n) continue;
            log(names.get(i) + ": Path: " + String.join(">", path[i]) + " Cost: " + dist[i]);
        }
    }

    public LSRCompute() {
        network = new Network();
    }

    public void log(String str) {
        log(str, "\n");
    }

    public void log(String str, String end) {
        System.out.print(str + end);
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 2) {
            throw new IllegalArgumentException("The format should be: java LSRCompute [filepath] [source_node] [SS|CA]");
        }

        LSRCompute lsr = new LSRCompute();

        lsr.load(new File(args[0]));
        lsr.dijkstra(args[1], (args.length > 2) && (args[2].equals("SS")));
    }
}
