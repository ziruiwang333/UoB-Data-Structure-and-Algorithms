package main;

import static org.junit.Assert.assertNotNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A node in a binary tree representing a Propositional Logic expression.
 * <p>
 * Each node has a type (AND node, OR node, NOT node etc.) and can have zero one
 * or two children as required by the node type (AND has two children, NOT has
 * one, TRUE, FALSE and variables have none
 * </p>
 * <p>
 * This class is mutable, and some of the operations are intended to modify the
 * tree internally. There are a few cases when copies need to be made of whole
 * sub-tree nodes in such a way that these copied trees do not share any nodes
 * with their originals. To do this the class implements a deep copying copy
 * constructor <code>PLTreeNode(PLTreeNode node)</code>
 * </p>
 * 
 */
public final class PLTreeNode implements PLTreeNodeInterface
{
	private static final Logger logger = Logger.getLogger(PLTreeNode.class);

	NodeType                    type;
	PLTreeNode                  child1;
	PLTreeNode                  child2;

	/**
	 * For marking purposes
	 * 
	 * @return Your student id
	 */
	public static String getStudentID()
	{
		//change this return value to return your student id number
		return "1935080";
	}

	/**
	 * For marking purposes
	 * 
	 * @return Your name
	 */
	public static String getStudentName()
	{
		//change this return value to return your name
		return "`Zirui Wang";
	}

	/**
	 * The default constructor should never be called and has been made private
	 */
	private PLTreeNode()
	{
		throw new RuntimeException("Error: default PLTreeNode constuctor called");
	}

	/**
	 * The usual constructor used internally in this class. No checks made on
	 * validity of parameters as this has been made private, so can only be
	 * called within this class.
	 * 
	 * @param type
	 *            The <code>NodeType</code> represented by this node
	 * @param child1
	 *            The first child, if there is one, or null if there isn't
	 * @param child2
	 *            The second child, if there is one, or null if there isn't
	 */
	private PLTreeNode(NodeType type, PLTreeNode child1, PLTreeNode child2)
	{
		// Don't need to do lots of tests because only this class can create nodes directly,
		// any construction required by another class has to go through reversePolishBuilder,
		// which does all the checks
		this.type = type;
		this.child1 = child1;
		this.child2 = child2;
	}

	/**
	 * A copy constructor to take a (recursive) deep copy of the sub-tree based
	 * on this node. Guarantees that no sub-node is shared with the original
	 * other
	 * 
	 * @param node
	 *            The node that should be deep copied
	 */
	private PLTreeNode(PLTreeNode node)
	{
		if (node == null)
			throw new RuntimeException("Error: tried to call the deep copy constructor on a null PLTreeNode");
		type = node.type;
		if (node.child1 == null)
			child1 = null;
		else
			child1 = new PLTreeNode(node.child1);
		if (node.child2 == null)
			child2 = null;
		else
			child2 = new PLTreeNode(node.child2);
	}


	/**
	 * Takes a list of <code>NodeType</code> values describing a valid
	 * propositional logic expression in reverse polish notation and constructs
	 * the corresponding expression tree. c.f. <a href=
	 * "https://en.wikipedia.org/wiki/Reverse_Polish_notation">https://en.wikipedia.org/wiki/Reverse_Polish_notation</a>
	 * <p>
	 * Thus an input containing
	 * </p>
	 * <pre>
	 * {NodeType.P, NodeType.Q, NodeType.NOT, NodeType.AND.
	 * </pre>
	 * 
	 * corresponds to
	 * 
	 * <pre>
	 * P∧¬Q
	 * </pre>
	 * 
	 * Leaving out the <code>NodeType</code> enum class specifier, we get that
	 * 
	 * <pre>
	 * { R, P, OR, TRUE, Q, NOT, AND, IMPLIES }
	 * </pre>
	 * 
	 * represents
	 * 
	 * <pre>
	 * ((R∨P)→(⊤∧¬Q))
	 * </pre>
	 * 
	 * @param typeList
	 *            An <code>NodeType</code> array in reverse polish order
	 * @return the <code>PLTreeNode</code> of the root of the tree representing
	 *         the expression constructed for the reverse polish order
	 *         <code>NodeType</code> array
	 */
	public static PLTreeNodeInterface reversePolishBuilder(NodeType[] typeList)
	{
		if (typeList == null || typeList.length == 0)
		{
			logger.error("Trying to create an empty PLTree");
			return null;
		}

		Deque<PLTreeNode> nodeStack = new LinkedList<>();

		for (NodeType type : typeList)
		{
			int arity = type.getArity();

			if (nodeStack.size() < arity)
			{
				logger.error(String.format(
						"Error: Malformed reverse polish type list: \"%s\" has arity %d, but is being applied to only %d arguments", //
						type, arity, nodeStack.size()));
				return null;
			}
			if (arity == 0)
				nodeStack.addFirst(new PLTreeNode(type, null, null));
			else if (arity == 1)
			{
				PLTreeNode node1 = nodeStack.removeFirst();
				nodeStack.addFirst(new PLTreeNode(type, node1, null));
			}
			else
			{
				PLTreeNode node2 = nodeStack.removeFirst();
				PLTreeNode node1 = nodeStack.removeFirst();
				nodeStack.addFirst(new PLTreeNode(type, node1, node2));
			}
		}
		if (nodeStack.size() > 1)
		{
			logger.error("Error: Incomplete term: multiple subterms not combined by top level symbol");
			return null;
		}

		return nodeStack.removeFirst();
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#getReversePolish()
	 */
	@Override
	public NodeType[] getReversePolish()
	{
		Deque<NodeType> nodeQueue = new LinkedList<>();

		getReversePolish(nodeQueue);

		return nodeQueue.toArray(new NodeType[0]);

	}

	/**
	 * A helper method for <code>getReversePolish()</code> used to accumulate
	 * the elements of the reverse polish notation description of the current
	 * tree
	 * 
	 * @param nodeQueue
	 *            A queue of <code>NodeType</code> objects used to accumulate
	 *            the values of the reverse polish notation description of the
	 *            current tree
	 */
	private void getReversePolish(Deque<NodeType> nodeQueue)
	{
		if (child1 != null)
			child1.getReversePolish(nodeQueue);
		if (child2 != null)
			child2.getReversePolish(nodeQueue);
		nodeQueue.addLast(type);
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#toString()
	 */
	@Override
	public String toString()
	{
		return toStringPrefix();
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#toStringPrefix()
	 */
	@Override
	public String toStringPrefix()
	{
		String statement = type.getPrefixName() + "(";
		if (child1 == null && child2 == null) {
			return type.getPrefixName();
		}
		if (child1 != null) {
			statement = statement + child1.toStringPrefix();
		}
		if(child2 != null) {
			if(child1 != null) statement = statement + ",";
			statement = statement + child2.toStringPrefix();
		}
		return statement + ")";
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#toStringInfix()
	 */
	@Override
	public String toStringInfix() {
		if (child1 == null && child2 == null) {
			return type.getInfixName();
		}
		if (child1 != null && child2 != null) {
			return "(" + child1.toStringInfix()  + type.getInfixName()  + child2.toStringInfix() + ")"; 
		}
		if (child1 != null && child2 == null) {
			return type.getInfixName() + child1.toStringInfix();
		}
		if (child1 == null && child2 != null) {
			return type.getInfixName() + child2.toStringInfix();
		}
		return null;
	}
		
//		String statement = "(";
//		if(child1!=null&&child2!=null) {
//			return type.getInfixName();
//		}
//		if(child1==null&&child2==null) {
//			statement=statement+"("+type.getInfixName()+")";
//		}
//		return statement;
//		
//}


	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#applyVarBindings(java.util.Map)
	 */
	@Override
	public void applyVarBindings(Map<NodeType, Boolean> bindings)
	{
		// WRITE YOUR CODE HERE
		if (bindings.containsKey(type)) {
			if(bindings.get(type)==true) {
				type=NodeType.TRUE;
			}
			else {
				type=NodeType.FALSE;
			}
		}
		if((child1!=null&&child2!=null)) {
			child1.applyVarBindings(bindings);
			child2.applyVarBindings(bindings);
		}
		if(child1!=null&&child2==null) {
			child1.applyVarBindings(bindings);
		}
		if(child1==null&&child2!=null) {
			child2.applyVarBindings(bindings);
		}
		return;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#evaluateConstantSubtrees()
	 */
	@Override
	public Boolean evaluateConstantSubtrees()
	{
		// WRITE YOUR CODE HERE, CHANGING THE RETURN STATEMENT IF NECESSARY
		
		return null;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#reduceToCNF()
	 */
	@Override
	public void reduceToCNF()
	{
		eliminateImplies();
		pushNotDown();
		pushOrBelowAnd();
		makeAndOrRightDeep();
		return;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#eliminateImplies()
	 */
	@Override
	public void eliminateImplies()
	{
		// WRITE YOUR CODE HERE
		if (type == NodeType.IMPLIES) {
			type = NodeType.OR;
			PLTreeNode notNode = new PLTreeNode(NodeType.NOT, child1, null);
			child1 = notNode;
		}
		if (child1 != null) {
			child1.eliminateImplies();
		}
		if (child2 != null) {
			child2.eliminateImplies();
		}
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#pushNotDown()
	 */
	@Override
	public void pushNotDown()
	{
		// WRITE YOUR CODE HERE
		
		// not not
		if (type == NodeType.NOT && child1.type == NodeType.NOT) {
			PLTreeNode tempNode = child1.child1;
			type = tempNode.type;
			child1 = tempNode.child1;
			child2 = tempNode.child2;
		}
		
		// not or
		if (type == NodeType.NOT && child1.type == NodeType.OR) {
			type = NodeType.AND;
			PLTreeNode x = new PLTreeNode(NodeType.NOT, child1.child1, null);
			PLTreeNode y = new PLTreeNode(NodeType.NOT, child1.child2, null);
			child1 = x;
			child2 = y;
		}
		
		// not and
		if (type == NodeType.NOT && child1.type == NodeType.AND) {
			type = NodeType.OR;
			PLTreeNode x = new PLTreeNode(NodeType.NOT, child1.child1, null);
			PLTreeNode y = new PLTreeNode(NodeType.NOT, child1.child2, null);
			child1 = x;
			child2 = y;
		}
		
		if (child1 != null) {
			child1.pushNotDown();
		}
		if (child2 != null) {
			child2.pushNotDown();
		}
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#pushOrBelowAnd()
	 */
	@Override
	public void pushOrBelowAnd()
	{
		// WRITE YOUR CODE HERE
		if (child1!=null&&child2!=null) {
			child1.pushOrBelowAnd();
			child2.pushOrBelowAnd();
		}
		if(child1!=null&&child2==null) {
			child1.pushOrBelowAnd();
		}
		if(child1==null&&child2!=null) {
			child2.pushOrBelowAnd();
		}
		if(type==NodeType.OR&&child1.type == NodeType.AND) {
			type=NodeType.AND;
			PLTreeNode aNode=new PLTreeNode(child1.child1);
			PLTreeNode bNode=new PLTreeNode(child1.child2);
			PLTreeNode cNode =new PLTreeNode(child2);
			PLTreeNode n1=new PLTreeNode(NodeType.OR, aNode, cNode);
			PLTreeNode n2=new PLTreeNode(NodeType.OR, bNode, cNode);
			child1=n1;
			child2=n2;
		}
		if (type==NodeType.OR&&child2.type==NodeType.AND) {
			type=NodeType.AND;
			PLTreeNode aNode=new PLTreeNode(child2.child1);
			PLTreeNode bNode=new PLTreeNode(child2.child2);
			PLTreeNode cNode =new PLTreeNode(child1);
			PLTreeNode n1=new PLTreeNode(NodeType.OR, cNode, aNode);
			PLTreeNode n2=new PLTreeNode(NodeType.OR, cNode, bNode);
			child1=n1;
			child2=n2;
		}
		return;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#makeAndOrRightDeep()
	 */
	@Override
	public void makeAndOrRightDeep()
	{
		return;
	}

}
