package see;

import Solver.ISolver;
import Solver.SolverResult;
import Solver.Z3Solver;
import cfg.ICFG;
import cfg.ICFGBasicBlockNode;
import cfg.ICFGDecisionNode;
import cfg.ICFGNode;
import expression.*;
import mycfg.CFGBasicBlockNode;
import mycfg.CFGDecisionNode;
import program.IProgram;
import set.*;
import statement.IStatement;
import utilities.Pair;

import java.util.*;

public class SEENew2 {
    private SET mSET;

    //  initialize newSEE using cfg
    public SEENew2(ICFG cfg) throws Exception {
        if (cfg != null) {
//            System.out.println(mSET.getNodeSet());
            this.mSET = new SET(cfg);
            Set<SETNode> nodeSet = mSET.getNodeSet();
            for (SETNode setNode : nodeSet) {
//                System.out.println("Node:" + setNode);
//                System.out.println("CFGNode:"+setNode.getCFGNode());
//            System.out.println("Head:"+setEdge.getHead());
//            System.out.println("Tail:"+setEdge.getTail());
            }
//            System.out.println(mSET.getNodeSet());
        } else {
            throw new Exception("Null CFG");
        }
    }

    public SET getSET() {
        return mSET;
    }

    public SETNode allPathSE(ICFG icfg, int depth) throws Exception {
        //  startnodeCFG
        ICFGBasicBlockNode startNode = icfg.getStartNode();
//        System.out.println(mSET.getNodeSet());
//        System.out.println(startNode);
        SETBasicBlockNode startSETNode = mSET.getStartNode();
//        System.out.println(mSET.getNodeSet());
        //  passing empty environment & startNode
//        SETNode startSETNode = this.singleStep(startNode,null);
//        System.out.println((SETBasicBlockNode)startSETNode);
//        System.out.println();
//        System.out.println(startSETNode);

//        System.out.println(mSET.getStartNode().getIncomingEdge());

        LinkedList<Pair<SETNode, Integer>> setNodeQueue = new LinkedList<>();
        setNodeQueue.add(new Pair<>(startSETNode, 1));
//        System.out.println(setNodeQueue.size());
        while (!setNodeQueue.isEmpty()) {
//            System.out.println(setNodeQueue.size());

            Pair<SETNode, Integer> pair = setNodeQueue.removeFirst();
//            System.out.println(setNodeQueue.size());

            Set<SETNode> nodeSet = mSET.getNodeSet();
            System.out.println(nodeSet);
            for (SETNode setNode:nodeSet){
                SETEdge incomingEdge = null;
                if(setNode.getIncomingEdge() != null) {
                    incomingEdge = setNode.getIncomingEdge();
                }
                System.out.println("NodeIncomingEdge:"+incomingEdge);
                System.out.println("Node:"+setNode);
    //            System.out.println("Head:"+setEdge.getHead());
    //            System.out.println("Tail:"+setEdge.getTail());
            }

            SETNode pairSETNode = pair.getFirst();
//            System.out.println(pairSETNode.getCFGNode());
//            System.out.println(pairSETNode);
            Integer pairDepth = pair.getSecond();
//            System.out.println(pairSETNode.getId());
//            System.out.println(pairDepth);

            if (pairDepth > depth) {
                continue;
            }
            ICFGNode correspondingICFGNode = pairSETNode.getCFGNode();
//            System.out.println(correspondingICFGNode);

            if (correspondingICFGNode instanceof ICFGBasicBlockNode) {
                ICFGNode successorNode = ((ICFGBasicBlockNode) correspondingICFGNode).getSuccessorNode();
//                System.out.println("successor:"+successorNode);

                if(successorNode!=null) {
                    SETNode setNode = singleStep(successorNode, pairSETNode);
                    setNodeQueue.add(new Pair<>(setNode, pairDepth + 1));
//                    System.out.println(setNodeQueue.size());
                }
            }

            if (correspondingICFGNode instanceof ICFGDecisionNode) {
                IExpression condition = ((ICFGDecisionNode) correspondingICFGNode).getCondition();

                AndExpression andExpression1 = new AndExpression(this.getSET(), pairSETNode.getPathPredicate(), condition);
                Set<IIdentifier> symVars = mSET.getVariables();
                ISolver solver = new Z3Solver(symVars, andExpression1);
                SolverResult solution = solver.solve();
                System.out.println(solution.getModel());
                //  if satisfiable
                if (solution.getResult() == true) {
                    ICFGNode thenNode = ((ICFGDecisionNode) correspondingICFGNode).getThenSuccessorNode();
//                    System.out.println("thenNode:"+thenNode);

                    if(thenNode!=null) {
                        SETNode setNode = singleStep(thenNode, pairSETNode);
                        setNodeQueue.add(new Pair<>(setNode, pairDepth + 1));
                    }
                }

                //  here we have to add "Not of the expression"
                NotExpression notExpression = new NotExpression(this.getSET(), condition);
                AndExpression andExpression2 = new AndExpression(this.getSET(), pairSETNode.getPathPredicate(), notExpression);
                Set<IIdentifier> symVars2 = mSET.getVariables();
                ISolver solver2 = new Z3Solver(symVars2, andExpression2);
                SolverResult solution2 = solver2.solve();
                System.out.println(solution.getModel());
                //  if unsatisfiable
                if (solution2.getResult() == true) {
                    ICFGNode elseNode = ((ICFGDecisionNode) correspondingICFGNode).getElseSuccessorNode();
//                    System.out.println("elseNode:"+elseNode);

                    if(elseNode!=null) {
                        SETNode setNode = singleStep(elseNode, pairSETNode);
                        setNodeQueue.add(new Pair<>(setNode, pairDepth + 1));
                    }
                }
            }
        }
        return startSETNode;
    }


    public SETNode singleStep(ICFGNode icfgNode, SETNode prevSetNode) throws Exception {
        //  for returning SETNode
        SETNode returnSETNode = null;

        //  naya edge banao
        //  head is null, for just now, it will be initialised below soon.
        SETEdge newSETEdge = new SETEdge(mSET, prevSetNode, null);

        //  if icfgNode is BasicBlockNode
        if (icfgNode instanceof ICFGBasicBlockNode) {
            //  startNode hai toh, sidha wohi node return kardo
            //  tail upar hi set ho gaya hai, head ke liye naya node banao
            returnSETNode = addNewSETBasicBlockNode(icfgNode, newSETEdge);
//            System.out.println(mSET.getNodeSet());
//            System.out.println(mSET.getEdgeSet());
        }

        //  if icfgNode is DecisionNode
        else if (icfgNode instanceof ICFGDecisionNode) {
            //  tail upar hi set ho gaya hai, head ke liye naya node banao
            returnSETNode = addNewSETDecisionNode(icfgNode, newSETEdge);
        }
        //  else case for escaping null pointer exception
        return returnSETNode;
    }

    //  function to add SETBasicBlockNode
    public SETBasicBlockNode addNewSETBasicBlockNode(ICFGNode newNode, SETEdge newSETEdge) throws Exception {
        SETBasicBlockNode newSETNode = new SETBasicBlockNode(mSET,
                (CFGBasicBlockNode) newNode);
//        System.out.println("new node:" + newSETNode);
        this.mSET.addBasicBlockNode(newSETNode);
//        System.out.println(mSET.getNodeSet());
//        System.out.println("newNodeSet:"+mSET.getEdgeSet());
        newSETEdge.setHead(newSETNode);
        newSETNode.setIncomingEdge(newSETEdge);
        this.mSET.addEdge(newSETEdge);
//        System.out.println(mSET.getEdgeSet());
        this.computeStatementList(newSETNode);
        return newSETNode;
    }

    //  function to add SETDecisionNode

    public SETDecisionNode addNewSETDecisionNode(ICFGNode newNode, SETEdge newSETEdge) throws Exception {
        CFGDecisionNode decisionNode = (CFGDecisionNode) newNode;
        SETDecisionNode newSETNode = new SETDecisionNode(
                decisionNode.getCondition(), mSET, decisionNode);
        this.mSET.addDecisionNode(newSETNode);
        newSETEdge.setHead(newSETNode);
        newSETNode.setIncomingEdge(newSETEdge);
        this.mSET.addEdge(newSETEdge);
        this.computeExpression(newSETNode);
        return newSETNode;
    }

    public void computeStatementList(SETBasicBlockNode node) throws Exception {
        ICFGBasicBlockNode cfgBasicBlockNode = (ICFGBasicBlockNode) node
                .getCFGNode();
//        System.out.println(cfgBasicBlockNode);
        IStatement statement = cfgBasicBlockNode.getStatement();

        /**TODO
         * add condition for stopNode, won't have LHS & RHS
         */
        try {
            IIdentifier LHS = statement.getLHS();
            IExpression RHS = statement.getRHS();
//            System.out.println(LHS);
//            System.out.println(RHS);

            //  add variable to SET
            mSET.addVariable(LHS);
            SETExpressionVisitor visitor = new SETExpressionVisitor(node,
                    LHS.getType());

            visitor.visit(RHS);

            IExpression value = null;
            //  get the symbolic expression by visiting the RHS (top of stack)
            value = visitor.getValue();
//        System.out.println(value);

            IIdentifier var = LHS;
            //  set symbolic expression into the node
            node.setValue(var, value);
            Map<IIdentifier, IExpression> map = node.getValues();
//        for (Map.Entry<IIdentifier,IExpression> entry : map.entrySet())
//            System.out.println("Key = " + entry.getKey() +
//                    ", Value = " + entry.getValue());
//        System.out.println(map);

        } catch (NullPointerException npe) {
            System.out.println(node.getCFGNode());
            System.out.println("End node reached");
        }


    }

    public void computeExpression(SETDecisionNode node) throws Exception {
        SETExpressionVisitor visitor = new SETExpressionVisitor(node,
                Type.BOOLEAN);
        CFGDecisionNode cfgNode = (CFGDecisionNode) node.getCFGNode();
        IExpression conditionCFG = cfgNode.getCondition();
        IExpression conditionSET = node.getCondition();
        if (conditionSET == null) {
            throw new Exception("Null Expression");
        } else {
            visitor.visit(cfgNode.getCondition());
            IExpression value = visitor.getValue();
            node.setCondition(value);
        }
    }

}
