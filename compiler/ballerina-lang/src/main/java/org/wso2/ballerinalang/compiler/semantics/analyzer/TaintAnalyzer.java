/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.elements.TaintError;
import org.ballerinalang.model.elements.TaintRecord;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeofExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;

/**
 * Generate taint-table for each invokable node which describes the tainted status of retun values, depending on the
 * tainted status of parameters. Propagate tainted status of variables across the program and evaluate invocations.
 */
public class TaintAnalyzer  extends BLangNodeVisitor {
    private static final CompilerContext.Key<TaintAnalyzer> TAINT_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolEnv env;
    private SymbolEnv mainPkgEnv;
    private SymbolEnv currPkgEnv;
    private Names names;
    private SymbolEnter symbolEnter;
    private DiagnosticLog dlog;

    private boolean nonOverridingAnalysis;
    private boolean entryPointAnalysis;
    private boolean stopAnalysis;
    private boolean initialAnalysisComplete;
    private BlockedNode blockedNode;
    private List<Boolean> taintedStatusList;
    private List<Boolean> returnTaintedStatusList;
    private List<TaintError> taintErrorList = new ArrayList<>();
    private Set<BlockedNode> blockedNodeList = new HashSet<>();

    private static final String ANNOTATION_TAINTED = "tainted";
    private static final String ANNOTATION_UNTAINTED = "untainted";
    private static final String ANNOTATION_SENSITIVE = "sensitive";

    private static final int ALL_UNTAINTED_TABLE_ENTRY_INDEX = -1;
    private static final String MAIN_FUNCTION_NAME = "main";

    public static TaintAnalyzer getInstance(CompilerContext context) {
        TaintAnalyzer taintAnalyzer = context.get(TAINT_ANALYZER_KEY);
        if (taintAnalyzer == null) {
            taintAnalyzer = new TaintAnalyzer(context);
        }
        return taintAnalyzer;
    }

    public TaintAnalyzer(CompilerContext context) {
        context.put(TAINT_ANALYZER_KEY, this);
        this.names = Names.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        this.mainPkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        pkgNode.accept(this);
        return pkgNode;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TAINT_ANALYZE)) {
            return;
        }
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        SymbolEnv prevPkgEnv = this.currPkgEnv;
        this.currPkgEnv = pkgEnv;
        this.env = pkgEnv;

        pkgNode.imports.forEach(impPkgNode -> impPkgNode.accept(this));
        pkgNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
        // Do table generation for blocked invokables after all the import packages are scanned.
        if (this.mainPkgEnv.equals(pkgEnv)) {
            initialAnalysisComplete = true;
            resolveBlockedInvokable();
            initialAnalysisComplete = false;
        }
        this.currPkgEnv = prevPkgEnv;
        pkgNode.completedPhases.add(CompilerPhase.TAINT_ANALYZE);
    }

    public void visit(BLangCompilationUnit compUnit) {
        compUnit.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        /* ignore */
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }
        this.env = pkgEnv;
        pkgEnv.node.accept(this);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI.accept(this);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        if (isEntryPoint(funcNode)) {
            visitEntryPoint(funcNode, funcEnv);
        } else {
            visitInvokable(funcNode, funcEnv);
        }
    }

    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, env);
        serviceNode.vars.forEach(var -> analyzeNode(var, serviceEnv));
        analyzeNode(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(resource -> analyzeNode(resource, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        visitEntryPoint(resourceNode, resourceEnv);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        connectorNode.varDefs.forEach(var -> var.accept(this));
        analyzeNode(connectorNode.initFunction, connectorEnv);
        analyzeNode(connectorNode.initAction, connectorEnv);
        connectorNode.actions.forEach(action -> analyzeNode(action, connectorEnv));
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;
        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        visitInvokable(actionNode, actionEnv);
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, env);
        structNode.fields.forEach(field -> analyzeNode(field, structEnv));
    }

    public void visit(BLangEnum enumNode) {
        enumNode.symbol.tainted = false;
    }

    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        /* ignore */
    }

    public void visit(BLangVariable varNode) {
        int ownerSymTag = env.scope.owner.tag;
        if (varNode.expr != null) {
             SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);
            // If the variable is a package/service/connector level variable, we don't need to check types.
            // It will we done during the init-function of the respective construct is visited.
            if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE &&
                    (ownerSymTag & SymTag.SERVICE) != SymTag.SERVICE &&
                    (ownerSymTag & SymTag.CONNECTOR) != SymTag.CONNECTOR) {
                analyzeNode(varNode.expr, varInitEnv);
                setTaintedStatus(varNode, taintedStatusList.get(0));
            }
        }
    }

    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        analyzeNode(workerNode.body, workerEnv);
    }

    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    public void visit(BLangAnnotation annotationNode) {
        annotationNode = annotationNode;
        /* ignore */
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        /* ignore */
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        /* ignore */
    }

    public void visit(BLangTransformer transformerNode) {
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, env);
        visitInvokable(transformerNode, transformerEnv);
    }

    // Statements
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        for (BLangStatement stmt : blockNode.stmts) {
            if (stopAnalysis) {
                break;
            } else {
                analyzeNode(stmt, blockEnv);
            }
        }
        if (stopAnalysis) {
            stopAnalysis = false;
        }
    }

    public void visit(BLangVariableDef varDefNode) {
        varDefNode.var.accept(this);
    }

    public void visit(BLangAssignment assignNode) {
        assignNode.expr.accept(this);
        boolean multiReturnsHandledProperly = taintedStatusList.size() == assignNode.varRefs.size();
        // Propagate tainted status of each variable separately (when multi returns are used).
        for (int varIndex = 0; varIndex < assignNode.varRefs.size(); varIndex++) {
            BLangExpression varRefExpr = assignNode.varRefs.get(varIndex);
            boolean varTaintedStatus;
            if (multiReturnsHandledProperly) {
                varTaintedStatus = taintedStatusList.get(varIndex);
            } else {
                varTaintedStatus = taintedStatusList.get(0);
            }
            // Generate error if a global variable has been assigned with a tainted value.
            if (varTaintedStatus && varRefExpr instanceof BLangVariableReference) {
                BLangVariableReference varRef = (BLangVariableReference) varRefExpr;
                if (varRef.symbol != null && varRef.symbol.owner != null) {
                    if (varRef.symbol.owner instanceof BPackageSymbol
                            || SymbolKind.SERVICE.equals(varRef.symbol.owner.kind)
                            || SymbolKind.CONNECTOR.equals(varRef.symbol.owner.kind)) {
                        addTaintError(assignNode.pos, varRef.symbol.name.value,
                                DiagnosticCode.TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE);
                        return;
                    }
                }
            }
            // TODO: Re-evaluating the full data-set (array) when a change occur.
            if (varRefExpr instanceof BLangIndexBasedAccess) {
                nonOverridingAnalysis = true;
                updatedVarRefTaintedState((BLangIndexBasedAccess) varRefExpr, varTaintedStatus);
                nonOverridingAnalysis = false;
            } else if (varRefExpr instanceof BLangFieldBasedAccess) {
                BLangFieldBasedAccess fieldBasedAccessExpr = (BLangFieldBasedAccess) varRefExpr;
                // Propagate tainted status to fields, when field symbols are present (Example: structs).
                if (fieldBasedAccessExpr.symbol != null) {
                    setTaintedStatus(fieldBasedAccessExpr, varTaintedStatus);
                }
                nonOverridingAnalysis = true;
                updatedVarRefTaintedState(fieldBasedAccessExpr, varTaintedStatus);
                nonOverridingAnalysis = false;
            } else {
                BLangVariableReference varRef = (BLangVariableReference) varRefExpr;
                setTaintedStatus(varRef, varTaintedStatus);
            }
        }
    }

    private void updatedVarRefTaintedState(BLangVariableReference varRef, boolean taintedState) {
        if (varRef instanceof BLangSimpleVarRef) {
            setTaintedStatus(varRef, taintedState);
        } else if (varRef instanceof BLangIndexBasedAccess) {
            BLangIndexBasedAccess indexBasedAccessExpr = (BLangIndexBasedAccess) varRef;
            updatedVarRefTaintedState(indexBasedAccessExpr.expr, taintedState);
        } else if (varRef instanceof BLangFieldBasedAccess) {
            BLangFieldBasedAccess fieldBasedAccessExpr = (BLangFieldBasedAccess) varRef;
            updatedVarRefTaintedState(fieldBasedAccessExpr.expr, taintedState);
        }
    }

    public void visit(BLangBind bindNode) {
        /* ignore */
    }

    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    public void visit(BLangNext nextNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    public void visit(BLangReturn returnNode) {
        List<Boolean> returnTaintedStatus = new ArrayList<>();
        if (returnNode.namedReturnVariables == null) {
            // If named returns are not used, evaluate each expression to identify the tainted status.
            for (BLangExpression expr : returnNode.exprs) {
                expr.accept(this);
                returnTaintedStatus.addAll(taintedStatusList);
            }
        } else {
            // If named returns are used, report back the tainted status of each variable.
            for (BLangVariable var : returnNode.namedReturnVariables) {
                returnTaintedStatus.add(var.symbol.tainted);
            }
        }
        if (returnTaintedStatusList == null) {
            returnTaintedStatusList = returnTaintedStatus;
        } else {
            // If there are multiple return statements within the same code block, combine outcomes to decide the
            // collective tainted status of each return.
            for (int i = 0; i < returnTaintedStatusList.size(); i++) {
                if (returnTaintedStatus.get(i)) {
                    returnTaintedStatusList.set(i, true);
                }
            }
        }
        taintedStatusList = returnTaintedStatusList;
    }

    public void visit(BLangReturn.BLangWorkerReturn returnNode) {
        /* ignore */
    }

    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        analyzeNode(exprStmtNode.expr, stmtEnv);
    }

    public void visit(BLangIf ifNode) {
        ifNode.body.accept(this);
        nonOverridingAnalysis = true;
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
        nonOverridingAnalysis = false;
    }

    public void visit(BLangForeach foreach) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        // Propagate the tainted status of collection to foreach variables.
        foreach.collection.accept(this);
        if (taintedStatusList.get(0)) {
            foreach.varRefs.forEach(varRef -> setTaintedStatus((BLangVariableReference) varRef,
                    taintedStatusList.get(0)));
        }
        analyzeNode(foreach.body, blockEnv);
    }

    public void visit(BLangWhile whileNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(whileNode.body, env);
        analyzeNode(whileNode.body, blockEnv);
    }

    public void visit(BLangLock lockNode) {
        lockNode.body.accept(this);
    }

    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody.accept(this);
        nonOverridingAnalysis = true;
        if (transactionNode.failedBody != null) {
            transactionNode.failedBody.accept(this);
        }
        nonOverridingAnalysis = false;
    }

    public void visit(BLangTryCatchFinally tryNode) {
        tryNode.tryBody.accept(this);
        nonOverridingAnalysis = true;
        tryNode.catchBlocks.forEach(c -> c.accept(this));
        if (tryNode.finallyBody != null) {
            tryNode.finallyBody.accept(this);
        }
        nonOverridingAnalysis = false;
    }

    public void visit(BLangCatch catchNode) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(catchNode.body, env);
        analyzeNode(catchNode.body, catchBlockEnv);
    }

    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(worker -> worker.accept(this));
        nonOverridingAnalysis = true;
        if (forkJoin.joinedBody != null) {
            forkJoin.joinedBody.accept(this);
        }
        if (forkJoin.timeoutBody != null) {
            forkJoin.timeoutBody.accept(this);
        }
        nonOverridingAnalysis = false;
    }

    // Expressions

    public void visit(BLangLiteral literalExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        if (arrayLiteral.exprs.size() == 0) {
            // Empty arrays are untainted.
            setTaintedStatusList(false);
        } else {
            for (BLangExpression expr : arrayLiteral.exprs) {
                expr.accept(this);
                if (taintedStatusList.get(0)) {
                    break;
                }
            }
        }
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        boolean isTainted = false;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            keyValuePair.valueExpr.accept(this);
            // Update field symbols with tainted status of the individual field (Example: struct).
            if (keyValuePair.key.fieldSymbol != null) {
                if (!nonOverridingAnalysis) {
                    keyValuePair.key.fieldSymbol.tainted = taintedStatusList.get(0);
                } else if (nonOverridingAnalysis && !keyValuePair.key.fieldSymbol.tainted) {
                    keyValuePair.key.fieldSymbol.tainted = taintedStatusList.get(0);
                }
            }
            // Used to update the variable this literal is getting assigned to.
            if (taintedStatusList.get(0)) {
                isTainted = true;
            }
        }
        setTaintedStatusList(isTainted);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.symbol == null) {
            Name varName = names.fromIdNode(varRefExpr.variableName);
            if (varName != Names.IGNORE) {
                if (varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
                    setTaintedStatusList(varRefExpr.pkgSymbol.tainted);
                    return;
                }
            }
            setTaintedStatusList(false);
        } else {
            setTaintedStatusList(varRefExpr.symbol.tainted);
        }
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BType varRefType = fieldAccessExpr.expr.type;
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                //TODO: Improve to use field level tainted status.
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.MAP:
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.JSON:
                //TODO: Improve to use field level tainted status when JSON is backed with Struct.
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.ENUM:
                setTaintedStatusList(false);
                break;
        }
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.expr.accept(this);
    }

    public void visit(BLangInvocation invocationExpr) {
        BLangInvokableNode invokableNode = null;
        if (invocationExpr.functionPointerInvocation) {
            invokableNode = ((BVarSymbol) invocationExpr.symbol).node;
            if (invokableNode == null) {
                // Skip function pointers and assume returns of function pointer executions are untainted.
                // TODO: Check the ability of resolving function pointers and perform analysis.
                List<Boolean> returnTaintedStatus = new ArrayList<>();
                ((BInvokableType) invocationExpr.symbol.type).retTypes
                        .forEach(retType -> returnTaintedStatus.add(false));
                taintedStatusList = returnTaintedStatus;
                return;
            }
        } else if (invocationExpr.iterableOperationInvocation) {
            invocationExpr.expr.accept(this);
            if (taintedStatusList.get(0)) {
                if (invocationExpr.argExprs != null) {
                    invocationExpr.argExprs.forEach(argExpr -> {
                        // If argument of iterable operation is a lambda expression, propagate the tainted status
                        // to function parameters and validate function body.
                        if (argExpr instanceof BLangLambdaFunction) {
                            analyzeLambdaExpressions(invocationExpr, argExpr);
                        }
                    });
                }
            }
            return;
        } else {
            invokableNode = ((BInvokableSymbol) invocationExpr.symbol).node;
        }
        if (invokableNode.symbol.taintTable == null) {
            addToBlockedList(invocationExpr, invokableNode.retParams.size());
        } else {
            analyzeInvocation(invocationExpr, invokableNode);
        }
    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        //TODO: Evaluate annotations added in connector init parameters.
        setTaintedStatusList(false);
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.thenExpr.accept(this);
        boolean thenTaintedCheckResult = taintedStatusList.get(0);
        nonOverridingAnalysis = true;
        ternaryExpr.elseExpr.accept(this);
        nonOverridingAnalysis = false;
        boolean elseTaintedCheckResult = taintedStatusList.get(0);
        setTaintedStatusList(thenTaintedCheckResult || elseTaintedCheckResult);
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr.accept(this);
        boolean lhsTaintedCheckResult = taintedStatusList.get(0);
        binaryExpr.rhsExpr.accept(this);
        boolean rhsTaintedCheckResult = taintedStatusList.get(0);
        setTaintedStatusList(lhsTaintedCheckResult || rhsTaintedCheckResult);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        switch (unaryExpr.operator) {
            case TYPEOF:
                setTaintedStatusList(false);
                break;
            case LENGTHOF:
                setTaintedStatusList(false);
                break;
            case UNTAINT:
                setTaintedStatusList(false);
                break;
            default:
                unaryExpr.expr.accept(this);
                break;
        }
    }

    public void visit(BLangTypeofExpr accessExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangTypeCastExpr castExpr) {
        // Result of the cast is tainted if value being casted is tainted.
        castExpr.expr.accept(this);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        // Result of the conversion is tainted if value being converted is tainted.
        conversionExpr.expr.accept(this);
    }

    public void visit(BLangXMLQName xmlQName) {
        setTaintedStatusList(false);
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(xmlAttribute, env);
        xmlAttribute.name.accept(this);
        boolean attrNameTainedStatus = taintedStatusList.get(0);
        xmlAttribute.value.accept(this);
        boolean attrValueTainedStatus = taintedStatusList.get(0);
        setTaintedStatusList(attrNameTainedStatus || attrValueTainedStatus);
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(xmlElementLiteral, env);

        // Visit in-line namespace declarations
        boolean inLineNamespaceTainted = false;
        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && ((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                attribute.accept(this);
                attribute.symbol.tainted = taintedStatusList.get(0);
                if (attribute.symbol.tainted) {
                    inLineNamespaceTainted = true;
                }
            }
        }

        // Visit attributes.
        boolean attributesTainted = false;
        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && !((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                attribute.accept(this);
                attribute.symbol.tainted = taintedStatusList.get(0);
                if (attribute.symbol.tainted) {
                    attributesTainted = true;
                }
            }
        }

        // Visit the tag names
        xmlElementLiteral.startTagName.accept(this);
        boolean startTagTaintedStatus = taintedStatusList.get(0);
        boolean endTagTaintedStatus = false;
        if (xmlElementLiteral.endTagName != null) {
            xmlElementLiteral.endTagName.accept(this);
            endTagTaintedStatus = taintedStatusList.get(0);
        }
        boolean tagNamesTainted = startTagTaintedStatus || endTagTaintedStatus;

        // Visit the children
        boolean childrenTainted = false;
        for (BLangExpression expr : xmlElementLiteral.children) {
            expr.accept(this);
            if (taintedStatusList.get(0)) {
                childrenTainted = true;
            }
        }

        setTaintedStatusList(inLineNamespaceTainted || attributesTainted || tagNamesTainted || childrenTainted);
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        analyzeExprList(xmlTextLiteral.textFragments);
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        analyzeExprList(xmlCommentLiteral.textFragments);
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target.accept(this);
        if (!taintedStatusList.get(0)) {
            analyzeExprList(xmlProcInsLiteral.dataFragments);
        }
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        analyzeExprList(xmlQuotedString.textFragments);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        analyzeExprList(stringTemplateLiteral.exprs);
    }

    public void visit(BLangWorkerSend workerSendNode) {
        //TODO: Propagate the tainted status during worker interactions.
        workerSendNode.exprs.forEach(expr -> expr.accept(this));
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        //TODO: Propagate the tainted status during worker interactions.
        workerReceiveNode.exprs.forEach(expr -> expr.accept(this));
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        /* ignore */
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.expr.accept(this);
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        setTaintedStatusList(false);
    }

    // Private

    private <T extends BLangNode, U extends SymbolEnv> void analyzeNode(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
    }

    /**
     * If any one of the given expressions are tainted, the final result will be tainted.
     *
     * @param exprs List of expressions to analyze.
     */
    private void analyzeExprList(List<BLangExpression> exprs) {
        for (BLangExpression expr : exprs) {
            expr.accept(this);
            if (taintedStatusList.get(0)) {
                break;
            }
        }
    }

    private boolean hasAnnotation(BLangVariable variable, String expectedAnnotation) {
        return variable.annAttachments.stream()
                .filter(annotation -> annotation.annotationName.value.equals(expectedAnnotation)).count() > 0;
    }

    /**
     * Set tainted status of the variable. When non-overriding analysis is in progress, this will not override "tainted"
     * status with "untaintd" status. As an example, the "else" section of a "if-else" block, cannot change a value
     * marked "tainted" by the "if" block.
     *
     * @param varNode Variable node to be updated.
     * @param taintedStatus Tainted status.
     */
    private void setTaintedStatus(BLangVariable varNode, boolean taintedStatus) {
        if (!nonOverridingAnalysis) {
            varNode.symbol.tainted = taintedStatus;
        } else if (nonOverridingAnalysis && !varNode.symbol.tainted) {
            varNode.symbol.tainted = taintedStatus;
        }
    }

    /**
     * Set tainted status of the variable. When non-overriding analysis is in progress, this will not override "tainted"
     * status with "untaintd" status. As an example, the "else" section of a "if-else" block, cannot change a value
     * marked "tainted" by the "if" block.
     *
     * @param varNode Variable node to be updated.
     * @param taintedStatus Tainted status.
     */
    private void setTaintedStatus(BLangVariableReference varNode, boolean taintedStatus) {
        if (!nonOverridingAnalysis) {
            varNode.symbol.tainted = taintedStatus;
        } else if (nonOverridingAnalysis && !varNode.symbol.tainted) {
            varNode.symbol.tainted = taintedStatus;
        }
    }

    private void setTaintedStatusList(boolean taintedStatus) {
        List<Boolean> taintedStatusList = new ArrayList<>();
        taintedStatusList.add(taintedStatus);
        this.taintedStatusList = taintedStatusList;
    }

    // Private methods related to invokable node analysis and taint-table generation.

    private boolean isEntryPoint(BLangFunction funcNode) {
        // Service resources are handled through BLangResource visitor.
        boolean isMainFunction = false;
        if (funcNode.name.value.equals(MAIN_FUNCTION_NAME) && funcNode.symbol.params.size() == 1
                && funcNode.symbol.retParams.size() == 0) {
            BType paramType = funcNode.symbol.params.get(0).type;
            BArrayType arrayType = (BArrayType) paramType;
            if (paramType.tag == TypeTags.ARRAY && arrayType.eType.tag == TypeTags.STRING) {
                isMainFunction = true;
            }
        }
        return isMainFunction;
    }

    private void visitEntryPoint(BLangInvokableNode invNode, SymbolEnv funcEnv) {
        // Entry point input parameters are all tainted, since they contain user controlled data.
        if (invNode.params != null) {
            invNode.params.forEach(param -> param.symbol.tainted = true);
        }
        entryPointAnalysis = true;
        analyzeReturnTaintedStatus(invNode, funcEnv);
        entryPointAnalysis = false;
        boolean isBlocked = processBlockedNode(invNode);
        if (isBlocked) {
            return;
        } else {
            // Display errors only if scan of was fully complete, so that errors will not get duplicated.
            taintErrorList.forEach(error -> {
                this.dlog.error(error.pos, error.diagnosticCode, error.paramName);
            });
            taintErrorList.clear();
        }
        invNode.params.forEach(param -> param.symbol.tainted = false);
        invNode.symbol.taintTable = new HashMap<>();
    }

    private void visitInvokable(BLangInvokableNode invNode, SymbolEnv symbolEnv) {
        if (invNode.symbol.taintTable == null) {
            if (Symbols.isNative(invNode.symbol)) {
                attachTaintTableBasedOnAnnotations(invNode);
                return;
            }
            Map<Integer, TaintRecord> taintTable = new HashMap<>();
            returnTaintedStatusList = null;
            // Check the tainted status of return values when no parameter is tainted.
            analyzeReturnTaintedStatus(taintTable, invNode, symbolEnv, ALL_UNTAINTED_TABLE_ENTRY_INDEX);
            boolean isBlocked = processBlockedNode(invNode);
            if (isBlocked) {
                return;
            }
            List<BLangVariable> params = invNode.params;
            for (int paramIndex = 0; paramIndex < params.size(); paramIndex++) {
                BLangVariable param = params.get(paramIndex);
                // If parameter is sensitive, it is invalid to have a case where tainted status of parameter is true.
                if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                    continue;
                }
                returnTaintedStatusList = null;
                // Set each parameter "tainted", then analyze the body to observe the outcome of the function.
                analyzeReturnTaintedStatus(taintTable, invNode, symbolEnv, paramIndex);
            }
            invNode.params.forEach(param -> param.symbol.tainted = false);
            invNode.symbol.taintTable = taintTable;
        }
    }

    private void analyzeReturnTaintedStatus(Map<Integer, TaintRecord> taintTable, BLangInvokableNode invokableNode,
                                            SymbolEnv symbolEnv, int paramIndex) {
        if (invokableNode.params != null) {
            invokableNode.params.forEach(param -> param.symbol.tainted = false);
        }
        // Mark the given parameter "tainted".
        if (paramIndex != ALL_UNTAINTED_TABLE_ENTRY_INDEX) {
            invokableNode.params.get(paramIndex).symbol.tainted = true;
        }
        analyzeReturnTaintedStatus(invokableNode, symbolEnv);
        if (taintErrorList.size() > 0) {
            // When invocation returns an error (due to passing a tainted argument to a sensitive parameter) add current
            // error to the table for future reference.
            taintTable.put(paramIndex, new TaintRecord(null, new ArrayList<>(taintErrorList)));
            taintErrorList.clear();
        } else {
            if (invokableNode.retParams.size() == 0) {
                returnTaintedStatusList = new ArrayList<>();
            } else {
                updatedReturnTaintedStatusBasedOnAnnotations(invokableNode.retParams);
            }
            taintTable.put(paramIndex, new TaintRecord(returnTaintedStatusList, null));
        }
    }

    private void analyzeReturnTaintedStatus(BLangInvokableNode invokableNode, SymbolEnv symbolEnv) {
        if (invokableNode.workers.isEmpty()) {
            analyzeNode(invokableNode.body, symbolEnv);
        } else {
            for (BLangWorker worker : invokableNode.workers) {
                analyzeNode(worker, symbolEnv);
                if (this.blockedNode != null || taintErrorList.size() > 0) {
                    break;
                }
            }
        }
    }

    private void attachTaintTableBasedOnAnnotations(BLangInvokableNode invokableNode) {
        if (invokableNode.symbol.taintTable == null) {
            // Extract tainted status of the function by lookint at annotations added to returns.
            List<Boolean> retParamsTaintedStatus = new ArrayList<>();
            for (BLangVariable retParam : invokableNode.retParams) {
                retParamsTaintedStatus.add(hasAnnotation(retParam, ANNOTATION_TAINTED));
            }
            // Append taint table with tainted status when no parameter is tainted.
            Map<Integer, TaintRecord> taintTable = new HashMap<>();
            taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, new TaintRecord(retParamsTaintedStatus, null));
            if (invokableNode.params.size() > 0) {
                // Append taint table with tainted status when each parameter is tainted.
                List<BLangVariable> params = invokableNode.params;
                for (int paramIndex = 0; paramIndex < params.size(); paramIndex++) {
                    BLangVariable param = params.get(paramIndex);
                    // If parameter is sensitive, test for this parameter being tainted is invalid.
                    if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                        continue;
                    }
                    taintTable.put(paramIndex, new TaintRecord(retParamsTaintedStatus, null));
                }
            }
            invokableNode.symbol.taintTable = taintTable;
        }
    }

    private void updatedReturnTaintedStatusBasedOnAnnotations(List<BLangVariable> retParams) {
        if (returnTaintedStatusList != null) {
            for (int paramIndex = 0; paramIndex < retParams.size(); paramIndex++) {
                BLangVariable param = retParams.get(paramIndex);
                boolean observedReturnTaintedStatus = returnTaintedStatusList.get(paramIndex);
                if (observedReturnTaintedStatus) {
                    // If return is tainted, but return is marked as untainted, overwrite the value.
                    if (hasAnnotation(param, ANNOTATION_UNTAINTED)) {
                        returnTaintedStatusList.set(paramIndex, false);
                    }
                } else {
                    // If return is not tainted, but return is marked as tainted, overwrite the value.
                    if (hasAnnotation(param, ANNOTATION_TAINTED)) {
                        returnTaintedStatusList.set(paramIndex, true);
                    }
                }
            }
        } else {
            returnTaintedStatusList = new ArrayList<>();
        }
    }

    private boolean processBlockedNode(BLangInvokableNode invokableNode) {
        boolean isBlocked = false;
        if (this.blockedNode != null) {
            // Add the function being blocked into the blocked node list for later processing.
            this.blockedNode.invokableNode = invokableNode;
            if (!initialAnalysisComplete) {
                blockedNodeList.add(blockedNode);
            }
            this.blockedNode = null;
            // Discard any error generated if invokable was found to be blocked. This will avoid duplicates when
            // blocked invokable is re-examined.
            taintErrorList.clear();
            isBlocked = true;
        }
        return isBlocked;
    }

    private void analyzeLambdaExpressions(BLangInvocation invocationExpr, BLangExpression argExpr) {
        BLangFunction function = ((BLangLambdaFunction) argExpr).function;
        if (function.symbol.taintTable == null) {
            addToBlockedList(invocationExpr, 1);
        } else {
            Map<Integer, TaintRecord> taintTable = function.symbol.taintTable;
            for (int paramIndex = 0; paramIndex < function.params.size(); paramIndex++) {
                TaintRecord taintRecord = taintTable.get(paramIndex);
                if (taintRecord == null) {
                    addTaintError(invocationExpr.pos, function.params.get(paramIndex).name.value,
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER);
                } else if (taintRecord.taintError != null && taintRecord.taintError.size() > 0) {
                    addTaintError(taintRecord.taintError);
                }
                if (stopAnalysis) {
                    break;
                }
            }
        }
    }

    private void addTaintError(DiagnosticPos diagnosticPos, String paramName, DiagnosticCode diagnosticCode) {
        TaintError taintError = new TaintError(diagnosticPos, paramName, diagnosticCode);
        taintErrorList.add(taintError);
        if (!entryPointAnalysis) {
            stopAnalysis = true;
        }
    }

    private void addTaintError(List<TaintError> taintError) {
        taintErrorList.addAll(taintError);
        if (!entryPointAnalysis) {
            stopAnalysis = true;
        }
    }

    private void addToBlockedList(BLangInvocation invocationExpr, int returnCount) {
        BlockingNode blockingNode = new BlockingNode(invocationExpr.symbol.pkgID, invocationExpr.symbol.name);
        this.blockedNode = new BlockedNode(this.currPkgEnv, null, invocationExpr.pos, blockingNode);
        stopAnalysis = true;
        List<Boolean> returnTaintedStatus = new ArrayList<>();
        for (int i = 0; i < returnCount; i++) {
            returnTaintedStatus.add(false);
        }
        taintedStatusList = returnTaintedStatus;
    }

    // Private methods relevant to invocation analysis.

    private void analyzeInvocation(BLangInvocation invocationExpr, BLangInvokableNode invokableNode) {
        Map<Integer, TaintRecord> taintTable = invokableNode.symbol.taintTable;
        List<Boolean> returnTaintedStatus = new ArrayList<>();
        TaintRecord allParamsUntaintedRecord = taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);
        if (allParamsUntaintedRecord.taintError != null && allParamsUntaintedRecord.taintError.size() > 0) {
            // This can occur when there is a error regardless of tainted status of parameters.
            // Example: Tainted value returned by function is passed to another functions's sensitive parameter.
            addTaintError(allParamsUntaintedRecord.taintError);
        } else {
            returnTaintedStatus = new ArrayList<>(taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX).retParamTaintdStatus);
        }
        if (invocationExpr.argExprs != null) {
            for (int argIndex = 0; argIndex < invocationExpr.argExprs.size(); argIndex++) {
                BLangExpression argExpr = invocationExpr.argExprs.get(argIndex);
                argExpr.accept(this);
                // If current argument is tainted, look-up the taint-table for the record of
                // return-tainted-status when the given argument is in tainted state.
                if (taintedStatusList.get(0)) {
                    TaintRecord taintRecord = taintTable.get(argIndex);
                    if (taintRecord == null) {
                        // This is when current parameter is "sensitive". Therefore, providing a tainted
                        // value to a sensitive parameter is invalid and should return a compiler error.
                        addTaintError(invocationExpr.pos, invokableNode.params.get(argIndex).name.value,
                                DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER);
                    } else if (taintRecord.taintError != null && taintRecord.taintError.size() > 0) {
                        // This is when current parameter is derived to be sensitive. The error already generated
                        // during taint-table generation will be used.
                        addTaintError(taintRecord.taintError);
                    } else {
                        // Go through tainted status of returns for each "tainted" parameter value. Combine tainted
                        // status of all returns to get accumulated tainted status of all returns for the invocation.
                        for (int returnIndex = 0; returnIndex < returnTaintedStatus.size(); returnIndex++) {
                            if (taintRecord.retParamTaintdStatus.get(returnIndex)) {
                                returnTaintedStatus.set(returnIndex, true);
                            }
                        }
                    }
                    if (stopAnalysis) {
                        break;
                    }
                }
            }
        }
        if (invocationExpr.expr != null) {
            // When an invocation like stringValue.trim() happens, if stringValue is tainted, the result will
            // also be tainted, unless it was explicitly marked untainted using @untainted with returns.
            invocationExpr.expr.accept(this);
            if (taintedStatusList.get(0)) {
                returnTaintedStatus = new ArrayList<>();
                for (BLangVariable retParam : invokableNode.retParams) {
                    if (hasAnnotation(retParam, ANNOTATION_UNTAINTED)) {
                        returnTaintedStatus.add(false);
                    } else {
                        returnTaintedStatus.add(true);
                    }
                }
            }
        }
        taintedStatusList = returnTaintedStatus;
    }

    private void resolveBlockedInvokable() {
        while (blockedNodeList.size() > 0) {
            Set<BlockedNode> remainingBlockedNodeList = new HashSet<>();
            for (BlockedNode blockedNode : blockedNodeList) {
                this.env = blockedNode.pkgSymbol;
                blockedNode.invokableNode.accept(this);
                if (blockedNode.invokableNode.symbol.taintTable == null) {
                    remainingBlockedNodeList.add(blockedNode);
                }
            }
            // If list is not moving, there is a recursion. Derive the tainted status of all the blocked
            // functions by using annotations and if annotations are not present generate error.
            if (blockedNodeList.size() == remainingBlockedNodeList.size()) {
                for (BlockedNode blockedNode : blockedNodeList) {
                    attachTaintTableBasedOnAnnotations(blockedNode.invokableNode);
                    boolean displayError = false;
                    for (BLangVariable retParam : blockedNode.invokableNode.retParams) {
                        if (!hasAnnotation(retParam, ANNOTATION_TAINTED)
                                && !hasAnnotation(retParam, ANNOTATION_UNTAINTED)) {
                            displayError = true;
                            break;
                        }
                    }
                    if (displayError) {
                        this.dlog.error(blockedNode.blockedPos, DiagnosticCode.TAINT_CHECKING_WITH_RECURSION,
                                blockedNode.invokableNode.name.value);
                    }
                }
            }
            blockedNodeList = remainingBlockedNodeList;
        }
    }
    
    private class BlockingNode {
        public PackageID packageID;
        public Name name;

        public BlockingNode(PackageID packageID, Name name) {
            this.packageID = packageID;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BlockingNode that = (BlockingNode) o;

            if (!packageID.equals(that.packageID)) {
                return false;
            }
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = packageID.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    private class BlockedNode {
        public BlockingNode blockingNode;
        public SymbolEnv pkgSymbol;
        public BLangInvokableNode invokableNode;
        public DiagnosticPos blockedPos;

        public BlockedNode(SymbolEnv pkgSymbol, BLangInvokableNode invokableNode, DiagnosticPos blockedPos,
                           BlockingNode blockingNode) {
            this.pkgSymbol = pkgSymbol;
            this.invokableNode = invokableNode;
            this.blockedPos = blockedPos;
            this.blockingNode = blockingNode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BlockedNode that = (BlockedNode) o;

            return invokableNode.symbol.pkgID.equals(that.invokableNode.symbol.pkgID) &&
                    invokableNode.symbol.name.equals(that.invokableNode.symbol.name);
        }

        @Override
        public int hashCode() {
            int result = invokableNode.symbol.pkgID.hashCode();
            result = 31 * result + invokableNode.symbol.name.hashCode();
            return result;
        }
    }
}
