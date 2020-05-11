package dk.brics.tajs.solver;

import java.util.List;
import java.util.Map;
import java.util.Comparator;

import java.util.stream.Collectors;

import java.io.PrintWriter;

import dk.brics.tajs.lattice.Value;

import dk.brics.tajs.flowgraph.SourceLocation;

import dk.brics.tajs.util.CallbackGraphAnalysisException;

import static dk.brics.tajs.solver.CallbackGraph.CallbackGraphNode;

public class CallbackGraphAnalysis {

	private CallbackGraph cbg;

	public CallbackGraphAnalysis( CallbackGraph cbg ) {
		this.cbg = cbg;
	}

	private static class ValueLocationPosition {

		private int line;

		private int column;

		ValueLocationPosition( int line, int column ) {
			this.line = line;
			
			this.column = column;
		}

		public int getLine() {
			return this.line;
		}

		public int getColumn() {
			return this.column;
		}

		@Override
		public String toString() {
			return this.line + ":" + this.column;
		}

	}

	/**
	 * Groups all the nodes on the {@code CallbackGraph} through a mapping
	 * of their {@code queueObject} to their {@code dependentQueueObject}.
	 * <p>
	 * Several nodes may contain the same {@code queueObject}. As such, each
	 * {@code dependentQueueObject} is grouped by the {@code queueObject}.
	 * @return A mapping of the node's {@code queueObject} to each respective
	 * {@code dependentQueueObject}
	 */
	private Map<Value, List<Value>> groupSourceNodesByQueueObject() {
		return this.cbg.getAllCallbacks().stream()
			.filter( n -> !n.getSecond().getQueueObject().getObjectLabelUnique().isHostObject() )
			.collect(
				Collectors.groupingBy(
					( CallbackGraphNode n ) -> n.getSecond().getQueueObject(),
					Collectors.mapping(
						( CallbackGraphNode n ) -> n.getSecond().getDependentQueueObject(),
						Collectors.toList()
					)
				)
			)
		;
	}

	/**
	 * Auxiliary method to get the line number for either
	 * a {@code queueObject} or a {@code dependentQueueObject}.
	 * <p>
	 * Checks if the {@code Value} has multiple locations.
	 * @param eitherQOrR either a {@code queueObject} or a {@code dependentQueueObject}.
	 * @return The line number for the {@code Value}.
	 */
	private ValueLocationPosition getValueLocation( Value eitherQOrR ) {
		if ( !eitherQOrR.isMaybeSingleAllocationSite() ) {
			throw new CallbackGraphAnalysisException(
				"Multiple source locations for value: " + eitherQOrR
			);
		}

		SourceLocation sl = eitherQOrR.getObjectSourceLocations().stream()
			.findFirst()
			.orElseThrow( () -> new CallbackGraphAnalysisException(
				"No source location found for value: " + eitherQOrR
			) )
		;

		return new ValueLocationPosition(
			sl.getLineNumber(),
			sl.getColumnNumber()
		);
	}

	/**
	 * Performs analysis on the CallbackGraph in order to look
	 * for any suspicion of broken Promises.
	 * <p>
	 * It operates by checking if there are multiple source nodes
	 * with the same {@code queueObject}, indicating there are several
	 * Promises chaining to the same Promise. 
	 * @return String representation of the warnings issued, if any.
	 */
	public String findBrokenPromise( PrintWriter out ) {
		StringBuilder warnings = this.groupSourceNodesByQueueObject()
			.entrySet()
			.stream()
			.filter( entry -> entry.getValue().size() > 1 )
			.collect(
				StringBuilder::new,
				( warningsSb, entry ) -> {
					Value queueObject = entry.getKey();

					List<Value> dependentQueueObjects = entry.getValue();

					List<String> positions = dependentQueueObjects.stream()
						.map( this::getValueLocation )
						.sorted(
							Comparator
								.comparing( ValueLocationPosition::getLine )
								.thenComparing( ValueLocationPosition::getColumn )
						)
						.map( String::valueOf )
						.collect( Collectors.toList() )
					;

					int lastIndex = positions.size() - 1;

					warningsSb
						.append( "Possible Broken Promise between positions: " )
						.append( String.join(
							", ",
							positions.subList( 0, lastIndex )
						) )
						.append( " and " )
						.append( positions.get( lastIndex ) )
						.append( "!\n" )
						.append( "Forked from position: " )
						.append( this.getValueLocation( queueObject ) )
						.append( ".\n\n" )
					;
				},
				StringBuilder::append
			)
		;

		out.println( warnings );

		return warnings.toString();
	}

	/*public String findUnexpectedExecutionOrder() {
		//
	}*/
}