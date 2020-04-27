package dk.brics.tajs.solver;

import java.util.List;
import java.util.ArrayList;

import java.util.stream.Collectors;

import java.io.PrintWriter;

import dk.brics.tajs.lattice.Value;
import dk.brics.tajs.analysis.AnalysisFunction;

import static dk.brics.tajs.solver.CallbackGraph.CallbackGraphNode;

public class CallbackGraphAnalysis {

	private CallbackGraph cbg;

	public CallbackGraphAnalysis( CallbackGraph cbg ) {
		this.cbg = cbg;
	}

	/**
	 * Groups all the nodes on the CallbackGraph by {@code Q}
	 * ({@code queueObject} field of its {@code CallbackContext}).
	 * @return A list containing lists for each different {@code Q}
	 */
	private List<List<CallbackGraphNode>> groupSourceNodesByQ() {
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
	 * Given a {@code CallbackGraphNode} retrieves the line of the
	 * associated callback.
	 * @param n The node to get the callback line from.
	 * @return The line of the callback invocation.
	 */
	private String getFunctionLineNumber( CallbackGraphNode n ) {
		AnalysisFunction function = n.getFirst();

		if ( !function.isNative() ) {
			return String.valueOf( function.getUserFunction().getSourceLocation().getLineNumber() );
		} else {
			return "??"; // TODO: find line number!
			//throw new RuntimeException( "Native function " + function.getNativeFunction().toString() );
		}
	}

	/**
	 * Performs analysis on the CallbackGraph in order to look
	 * for any suspicion of broken Promises.
	 * <p>
	 * It operates by checking if there are multiple source nodes
	 * with the same {@code Q}.
	 * @return TODO
	 */
	public String findBrokenPromise( PrintWriter out ) {
		StringBuilder warnings = new StringBuilder();

		List<List<CallbackGraphNode>> groupedNodesByQ = this.groupSourceNodesByQ();

		for ( List<CallbackGraphNode> groupOfNodes : groupedNodesByQ ) {
			if ( groupOfNodes.size() > 1 ) {
				int lastIndex = groupOfNodes.size() - 1;

				warnings.append( "Possible Broken Promise between lines: " );

				String formattedLinesNumbers = groupOfNodes.subList( 0, lastIndex ).stream()
					.map( this::getFunctionLineNumber )
					.collect( Collectors.joining( ", " ) )
				;

				warnings
					.append( formattedLinesNumbers )
					.append( " and " )
					.append( this.getFunctionLineNumber( groupOfNodes.get( lastIndex ) ) )
					.append( "!\n" )
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