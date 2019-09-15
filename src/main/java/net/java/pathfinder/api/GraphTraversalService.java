package net.java.pathfinder.api;

import net.java.pathfinder.internal.GraphDao;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Stateless
@Path("/graph-traversal")
public class GraphTraversalService {

    @Inject
    private GraphDao dao;
    private final Random random = new Random();

    @GET
    @Path("/shortest-path")
    @Produces("application/json")
    // TODO Add internationalized messages for constraints.
    public List<TransitPath> findShortestPath(
            @NotNull @Size(min = 5, max = 5) @QueryParam("origin") String originUnLocode,
            @NotNull @Size(min = 5, max = 5) @QueryParam("destination") String destinationUnLocode,
            @QueryParam("deadline") String deadline) {
        LocalDateTime date = nextDate(LocalDateTime.now());

        List<String> allVertices = dao.listLocations();
        allVertices.remove(originUnLocode);
        allVertices.remove(destinationUnLocode);

        int candidateCount = getRandomNumberOfCandidates();
        List<TransitPath> candidates = new ArrayList<>(
                candidateCount);

        for (int i = 0; i < candidateCount; i++) {
            allVertices = getRandomChunkOfLocations(allVertices);
            List<TransitEdge> transitEdges = new ArrayList<>(
                    allVertices.size() - 1);
            String firstLegTo = allVertices.get(0);

            LocalDateTime fromDate = nextDate(date);
            LocalDateTime toDate = nextDate(fromDate);
            date = nextDate(toDate);

            transitEdges.add(new TransitEdge(
                    dao.getVoyageNumber(originUnLocode, firstLegTo),
                    originUnLocode, firstLegTo, fromDate.toLocalDate(), toDate.toLocalDate()));

            for (int j = 0; j < allVertices.size() - 1; j++) {
                String current = allVertices.get(j);
                String next = allVertices.get(j + 1);
                fromDate = nextDate(date);
                toDate = nextDate(fromDate);
                date = nextDate(toDate);
                transitEdges.add(new TransitEdge(dao.getVoyageNumber(current,
                        next), current, next, fromDate.toLocalDate(), toDate.toLocalDate()));
            }

            String lastLegFrom = allVertices.get(allVertices.size() - 1);
            fromDate = nextDate(date);
            toDate = nextDate(fromDate);
            transitEdges.add(new TransitEdge(
                    dao.getVoyageNumber(lastLegFrom, destinationUnLocode),
                    lastLegFrom, destinationUnLocode, fromDate.toLocalDate(), 
                    toDate.toLocalDate()));

            candidates.add(new TransitPath(transitEdges));
        }

        return candidates;
    }

    private LocalDateTime nextDate(LocalDateTime date) {
        return date.plusDays(1).plusMinutes((random.nextInt(1000) - 500));
    }

    private int getRandomNumberOfCandidates() {
        return 3 + random.nextInt(3);
    }

    private List<String> getRandomChunkOfLocations(List<String> allLocations) {
        Collections.shuffle(allLocations);
        int total = allLocations.size();
        int chunk = total > 4 ? 1 + new Random().nextInt(5) : total;
        return allLocations.subList(0, chunk);
    }
}
