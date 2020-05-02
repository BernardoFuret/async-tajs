package dk.brics.tajs.solver;

import java.util.List;
import java.util.ArrayList;

import java.util.stream.Collectors;

import java.io.PrintWriter;

import dk.brics.tajs.lattice.Value;

import dk.brics.tajs.flowgraph.SourceLocation;

import static dk.brics.tajs.solver.CallbackGraph.CallbackGraphNode;

public class CallbackGraphAnalysis { //TODO: refactor

	private CallbackGraph cbg;

	public CallbackGraphAnalysis( CallbackGraph cbg ) {
		this.cbg = cbg;
	}

	/**
	 * Groups all the nodes on the {@code CallbackGraph} by {@code queueObject}.
	 * @return A list containing lists for each different {@code queueObject}
	 */
	private List<List<CallbackGraphNode>> groupSourceNodesByQueueObject() {
		List<CallbackGraphNode> sourceNodes = new ArrayList<>( this.cbg.getAllCallbacks() );

		List<Integer> visitedIndexes = new ArrayList<>();

		List<List<CallbackGraphNode>> allNodesWithSameQ = new ArrayList<>();
		
		int outerIndex = 0;

		for ( CallbackGraphNode node : sourceNodes ) {
			Value nodeQ = node.getSecond().getQueueObject();

			if ( !visitedIndexes.contains( outerIndex ) && nodeQ != null ) {
				int innerIndex = 0;

				List<CallbackGraphNode> nodesWithSameQ = new ArrayList<>();

				for ( CallbackGraphNode comparedNode : sourceNodes ) {
					Value comparedNodeQ = comparedNode.getSecond().getQueueObject();

					if (
						comparedNodeQ != null
						&&
						!visitedIndexes.contains( innerIndex )
						&&
						comparedNodeQ.equals( nodeQ )
					) {
						nodesWithSameQ.add( comparedNode );

						visitedIndexes.add( innerIndex );
					}

					innerIndex += 1;
				}

				allNodesWithSameQ.add( nodesWithSameQ );
			}

			visitedIndexes.add( outerIndex );
			
			outerIndex += 1;
		}

		return allNodesWithSameQ;
	}

	/**
	 * Given a {@code CallbackGraphNode} retrieves the line of
	 * the dependent queue object.
	 * <p>
	 * That is: the line of the element being created by the
	 * invocation of the associated callback. 
	 * @param node The node to get the line from.
	 * @return The line of the element being created by the callback invocation.
	 */
	private String getDependentQueueObjectLineNumber( CallbackGraphNode node ) {
		Value dependentQueueObject = node.getSecond().getDependentQueueObject();

		return dependentQueueObject.getObjectSourceLocations().stream()
			.map( sl -> String.valueOf( sl.getLineNumber() ) )
			.collect( Collectors.joining( " / " ) )
		;
	}

	/**
	 * Given a {@code CallbackGraphNode} retrieves the line of
	 * the queue object.
	 * <p>
	 * That is: the line of the element to where this callback
	 * was registered. 
	 * @param n The node to get the line from.
	 * @return The line of the element where this callback registered to.
	 */
	private String getQueueObjectLineNumber( CallbackGraphNode node ) {
		Value queueObject = node.getSecond().getQueueObject();

		return queueObject.getObjectSourceLocations().stream()
			.map( sl -> String.valueOf( sl.getLineNumber() ) )
			.collect( Collectors.joining( " / " ) )
		;
	}

	/**
	 * Performs analysis on the CallbackGraph in order to look
	 * for any suspicion of broken Promises.
	 * <p>
	 * It operates by checking if there are multiple source nodes
	 * with the same {@code queueObject}.
	 * @return TODO
	 */
	public String findBrokenPromise( PrintWriter out ) {
		StringBuilder warnings = new StringBuilder();

		List<List<CallbackGraphNode>> groupedNodesByQ = this.groupSourceNodesByQueueObject();

		for ( List<CallbackGraphNode> groupOfNodes : groupedNodesByQ ) {
			if ( groupOfNodes.size() > 1 ) {
				int lastIndex = groupOfNodes.size() - 1;

				warnings.append( "Possible Broken Promise between lines: " );

				String formattedLinesNumbers = groupOfNodes.subList( 0, lastIndex ).stream()
					.map( this::getDependentQueueObjectLineNumber )
					.collect( Collectors.joining( ", " ) )
				;

				CallbackGraphNode lastNode = groupOfNodes.get( lastIndex );

				warnings
					.append( formattedLinesNumbers )
					.append( " and " )
					.append( this.getDependentQueueObjectLineNumber( lastNode ) )
					.append( "!\n" )
					.append( "Forked from line: " )
					.append( this.getQueueObjectLineNumber( lastNode ) ) // Could be any node of this group, since all of them have the same queueObject
					.append( ".\n" )
				;
			}
		}

        out.println( warnings );

		return warnings.toString();
	}

	/*public String findUnexpectedExecutionOrder() {
		//
	}*/
}