package testcases;

import cfg.ICFEdge;
import cfg.ICFG;
import cfg.ICFGBasicBlockNode;
import cfg.ICFGDecisionNode;
import expression.*;
import mycfg.CFEdge;
import mycfg.CFG;
import mycfg.CFGBasicBlockNode;
import mycfg.CFGDecisionNode;
import org.junit.Test;
import see.SEENew;
import see.SEENew2;
import set.SETBasicBlockNode;
import set.SETDecisionNode;
import set.SETEdge;
import set.SETNode;
import statement.Statement;

import java.util.Set;

public class TestNewSEE2 {
    @Test
    public void testNaya() throws Exception {
        // Why mCFG ? Because it is an inteface for Control Flow Graph
        ICFG mCFG = null;

        // Why A,W ?
        // A -> start node
        // W -> end node
        ICFGBasicBlockNode A = new CFGBasicBlockNode("A",null);
        ICFGBasicBlockNode W = new CFGBasicBlockNode("W",null);
        // This constructor initializes mCFG with start & end node
        mCFG = new CFG(A, W);

        ConcreteConstant CONSTANT_TWO = new ConcreteConstant(2,mCFG);
        ConcreteConstant CONSTANT_FIVE = new ConcreteConstant(5,mCFG);
        ConcreteConstant CONSTANT_TWENTY = new ConcreteConstant(20,mCFG);
        ConcreteConstant CONSTANT_THIRTY = new ConcreteConstant(30,mCFG);


        //  variables x & y
        Variable x = new Variable("x", mCFG);
        Variable y = new Variable("y", mCFG);

        ICFGBasicBlockNode B = new CFGBasicBlockNode("B", mCFG);
        Input i1 = new Input(mCFG);
        Statement stmt1 = new Statement(mCFG, x, i1);
        B.setStatement(stmt1);
        mCFG.addBasicBlockNode(B);

        ICFGBasicBlockNode C = new CFGBasicBlockNode("C", mCFG);
        Input i2 = new Input(mCFG);
        Statement stmt2 = new Statement(mCFG, y, i2);
        C.setStatement(stmt2);
        mCFG.addBasicBlockNode(C);

        LesserThanExpression expr3 = new LesserThanExpression(mCFG, x, y);
        NotExpression nexpr1 = new NotExpression(mCFG,expr3);
        ICFGDecisionNode D = new CFGDecisionNode(mCFG,nexpr1);
        mCFG.addDecisionNode(D);

        ICFGBasicBlockNode E = new CFGBasicBlockNode("E", mCFG);
        Statement stmt3 = new Statement(mCFG, x, CONSTANT_TWENTY);
        E.setStatement(stmt3);
        mCFG.addBasicBlockNode(E);

        ICFGBasicBlockNode F = new CFGBasicBlockNode("F", mCFG);
        Statement stmt4 = new Statement(mCFG, x, CONSTANT_THIRTY);
        F.setStatement(stmt4);
        mCFG.addBasicBlockNode(F);




//
//        ICFGBasicBlockNode C = new CFGBasicBlockNode("C", mCFG);
//        // Input i1 = new Input(mCFG)
//        MulExpression expr1 = new MulExpression(mCFG,CONSTANT_TWO,x);
//        AddExpression expr2 = new AddExpression(mCFG,expr1,CONSTANT_FIVE);
//        AddExpression expr3 = new AddExpression(mCFG,expr2,CONSTANT_TWO);
//        Statement stmt3 = new Statement(mCFG, y, expr3);
//        C.setStatement(stmt3);
//        mCFG.addBasicBlockNode(C);

        //edges
        ICFEdge AB = new CFEdge("AB", mCFG, A, B);
        ICFEdge BC = new CFEdge("BC", mCFG, B, C);
        ICFEdge CD = new CFEdge("CD", mCFG, C, D);
        ICFEdge DE = new CFEdge("DE", mCFG, D, E);
        ICFEdge DF = new CFEdge("DF", mCFG, D, F);
        ICFEdge EW = new CFEdge("EW", mCFG, E, W);
        ICFEdge FW = new CFEdge("FW", mCFG, F, W);

//        D.setThenEdge(DE);
//        D.setElseEdge(DF);

        SEENew2 seeNew2 = new SEENew2(mCFG);

        SETNode setNode6 = seeNew2.allPathSE(mCFG,5);

        System.out.println(seeNew2.getSET().getStartNode().getIncomingEdge());
        System.out.println(seeNew2.getSET().getStartNode().getCFGNode());

        Set<SETEdge> edgeSet = seeNew2.getSET().getEdgeSet();
        for (SETEdge setEdge:edgeSet){
            System.out.println("Edge:"+setEdge);
            System.out.println("Head:"+setEdge.getHead());
            System.out.println("Tail:"+setEdge.getTail().getIncomingEdge());
            System.out.println("Tail:"+setEdge.getTail().getCFGNode());
        }

//        Set<SETNode> nodeSet = seeNew2.getSET().getNodeSet();
//        for (SETNode setNode:nodeSet){
//            System.out.println("Node:"+setNode);
//            System.out.println("CFGNode:"+setNode.getCFGNode());
////            System.out.println("Head:"+setEdge.getHead());
////            System.out.println("Tail:"+setEdge.getTail());
//        }

//        //  passing empty environment & startNode
//        SETBasicBlockNode startNode = new SETBasicBlockNode(seeNew2.getSET(),A);
//
//        SETNode setNode = seeNew2.singleStep(B,startNode);
//        System.out.println(setNode.getLatestValue(x));
//
//        SETNode setNode2 = seeNew2.singleStep(C,setNode);
//        System.out.println(setNode2.getLatestValue(y));
//
//        SETNode setNode3 = seeNew2.singleStep(D,setNode2);
//        System.out.println("PathPredicate:"+((SETDecisionNode)setNode3).getCondition());
//
//        SETNode setNode4 = seeNew2.singleStep(E,setNode3);
//        System.out.println(setNode4.getLatestValue(x));
//
//        SETNode setNode5 = seeNew2.singleStep(F,setNode4);
//        System.out.println(setNode5.getLatestValue(x));

//        SETNode setNode6 = seeNew2.allPathSE(mCFG,5);
//        System.out.println(setNode6.getSET());

//        System.out.println(seeNew2.getSET().getNodeSet());

//        System.out.println(seeNew2.getSET().getNumberOfDecisionNodes());

//        SETNode setNode6 = seeNew2.allPathSE(mCFG,5);
//        System.out.println(setNode6);





    }
}