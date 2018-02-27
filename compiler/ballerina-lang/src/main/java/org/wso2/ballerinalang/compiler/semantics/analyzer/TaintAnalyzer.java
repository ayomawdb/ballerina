package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangTaint;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangUntaint;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
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
 * Perform taint analysis.
 */
public class TaintAnalyzer  extends BLangNodeVisitor {
    private static final CompilerContext.Key<TaintAnalyzer> TAINT_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private BLangPackage pkgNode;
    private SymbolEnv mainPkgEnv;
    private SymbolEnv currPkgSymbol;
    private Names names;
    private SymbolResolver symResolver;
    private SymbolEnter symbolEnter;
    private DiagnosticLog dlog;
    private SymbolEnv env;

    private List<Boolean> taintedStatus;
    private boolean nonOverridingContext;

    private boolean initialGenerationComplete;
    private TaintError taintError;
    private boolean stopAnalysis;
    private BlockedNode blockedNode;
    private boolean entrypointAnalysis;

    //TODO Improve with a stack (perf)
    private Set<BlockedNode> blockedNodes = new HashSet<>();

    private static final String MAIN_FUNCTION_NAME = "main";
    private static final int ALL_UNTAINTED_TABLE_ENTRY_INDEX = -1;

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
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        this.mainPkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        this.pkgNode = pkgNode;
        pkgNode.accept(this);
        return pkgNode;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TAINT_TABLE_ENTER)) {
            return;
        }
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        SymbolEnv prevPkgEnv = this.currPkgSymbol;
        this.currPkgSymbol = pkgEnv;
        this.env = pkgEnv;

        pkgNode.imports.forEach(impPkgNode -> impPkgNode.accept(this));
        pkgNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
        // Do table generation for blocked invokable, only after all the import packages are scanned.
        if (this.mainPkgEnv.equals(pkgEnv)) {
            initialGenerationComplete = true;
            resolveBlockedInvokables();
            initialGenerationComplete = false;
        }
        this.currPkgSymbol = prevPkgEnv;
        pkgNode.completedPhases.add(CompilerPhase.TAINT_TABLE_ENTER);
    }

    public void visit(BLangCompilationUnit compUnit) {
        //TODO: Test
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
            visitEntrypoint(funcNode, funcEnv);
        } else {
            visitInvocable(funcNode, funcEnv);
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
        visitEntrypoint(resourceNode, resourceEnv);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        //connectorNode.varDefs.forEach(varDef -> analyzeNode(varDef, connectorEnv));
        analyzeNode(connectorNode.initFunction, connectorEnv);
        analyzeNode(connectorNode.initAction, connectorEnv);
        connectorNode.actions.forEach(action -> analyzeNode(action, connectorEnv));
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;
        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        visitInvocable(actionNode, actionEnv);
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
        // Analyze the init expression
        if (varNode.expr != null) {
            // Here we create a new symbol environment to catch self references by keep the current
            // variable symbol in the symbol environment
            // e.g. int a = x + a;
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);
            // If the variable is a package/service/connector level variable, we don't need to check types.
            // It will we done during the init-function of the respective construct is visited.
            if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE &&
                    (ownerSymTag & SymTag.SERVICE) != SymTag.SERVICE &&
                    (ownerSymTag & SymTag.CONNECTOR) != SymTag.CONNECTOR) {
                if (varNode.expr instanceof BLangConnectorInit) {
                    analyzeNode(varNode.expr, varInitEnv);
                } else {
                    analyzeNode(varNode.expr, varInitEnv);
                }
                setTaintedStatus(varNode, taintedStatus.get(0));
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
        //TODO
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, env);
        visitInvocable(transformerNode, transformerEnv);
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
        // True when second return of a multi return skipped.
        boolean multiReturnHandledProperly = assignNode.varRefs != null && taintedStatus.size() ==
                assignNode.varRefs.size();
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression expr = assignNode.varRefs.get(i);
            boolean taintedCheckResult = taintedStatus.get(0);
            if (multiReturnHandledProperly) {
                taintedStatus.get(i);
            }
            if (expr instanceof BLangIndexBasedAccess) {
                // TODO: Check the possibility of re-evaluating the full dataset (array) when a change occur
                nonOverridingContext = true;
                setTaintedStatus(((BLangIndexBasedAccess) expr).expr, taintedCheckResult);
                nonOverridingContext = false;
            } else if (expr instanceof BLangFieldBasedAccess) {
                // TODO: Check the possibility of re-evaluating the full dataset (array) when a change occur
                if (((BLangFieldBasedAccess) expr).symbol != null) {
                    setTaintedStatus((BLangFieldBasedAccess) expr, taintedCheckResult);
                } else {
                    nonOverridingContext = true;
                    setTaintedStatus(((BLangFieldBasedAccess) expr).expr, taintedCheckResult);
                    nonOverridingContext = false;
                }
            } else {
                setTaintedStatus((BLangVariableReference) expr, taintedCheckResult);
            }
        }
    }

    public void visit(BLangBind bindNode) {
        bindNode.expr.accept(this);
        if (bindNode.varRef instanceof BLangVariableReference) {
            setTaintedStatus((BLangVariableReference) bindNode.varRef, taintedStatus.get(0));
        }
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
        List<Boolean> taintedCheckResults = new ArrayList<>();
        if (returnNode.namedReturnVariables == null) {
            for (BLangExpression expr : returnNode.exprs) {
                expr.accept(this);
                taintedCheckResults.add(taintedStatus.get(0));
            }
        } else {
            for (BLangVariable var : returnNode.namedReturnVariables) {
                taintedCheckResults.add(var.symbol.tainted);
            }
        }
        taintedStatus = taintedCheckResults;
    }

    public void visit(BLangReturn.BLangWorkerReturn returnNode) {
        /* ignore */
    }

    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        /* ignore */
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        analyzeNode(exprStmtNode.expr, stmtEnv);
    }

    public void visit(BLangIf ifNode) {
        ifNode.body.accept(this);
        nonOverridingContext = true;
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
        nonOverridingContext = false;
    }

    public void visit(BLangForeach foreach) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
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
        nonOverridingContext = true;
        if (transactionNode.failedBody != null) {
            transactionNode.failedBody.accept(this);
        }
        nonOverridingContext = false;
    }

    public void visit(BLangTryCatchFinally tryNode) {
        tryNode.tryBody.accept(this);
        nonOverridingContext = true;
        tryNode.catchBlocks.forEach(c -> c.accept(this));
        if (tryNode.finallyBody != null) {
            tryNode.finallyBody.accept(this);
        }
        nonOverridingContext = true;
    }

    public void visit(BLangCatch catchNode) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(catchNode.body, env);
        analyzeNode(catchNode.body, catchBlockEnv);
    }

    public void visit(BLangForkJoin forkJoin) {
        /* ignore */
    }

    public void visit(BLangTaint taint) {
        /* ignore */
    }

    public void visit(BLangUntaint untaint) {
        /* ignore */
    }

    // Expressions

    public void visit(BLangLiteral literalExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        for (BLangExpression expr : arrayLiteral.exprs) {
            expr.accept(this);
            if (taintedStatus.get(0)) {
                break;
            }
        }
        setTaintedStatusList(taintedStatus.get(0));
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        boolean isTainted = false;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            keyValuePair.valueExpr.accept(this);

            // Make sure the symbol for key value pair is updated with the final decision if the pair is in symbol table
            if (keyValuePair.key.fieldSymbol != null) {
                keyValuePair.key.fieldSymbol.tainted = taintedStatus.get(0);
            }
            // To update the tainted status of the variable
            if (taintedStatus.get(0)) {
                isTainted = true;
            }
        }
        setTaintedStatusList(isTainted);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.symbol == null) {
            Name varName = names.fromIdNode(varRefExpr.variableName);
            if (varName != Names.IGNORE) {
                BSymbol symbol = symResolver.lookupSymbolInPackage(varRefExpr.pos, env,
                        names.fromIdNode(varRefExpr.pkgAlias), varName, SymTag.VARIABLE_NAME);
                setTaintedStatusList(symbol.tainted);
            }
        } else {
            setTaintedStatusList(varRefExpr.symbol.tainted);
        }
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BType varRefType = fieldAccessExpr.expr.type;
        Boolean isTainted = false;
        Name fieldName = names.fromIdNode(fieldAccessExpr.field);
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                BSymbol fieldSymbol = symResolver.resolveStructField(fieldAccessExpr.pos, this.env,
                        fieldName, varRefType.tsymbol);
                isTainted = fieldSymbol.tainted;
                break;
            case TypeTags.MAP:
                isTainted = fieldAccessExpr.expr.symbol.tainted;
                break;
            case TypeTags.JSON:
                BType constraintType = ((BJSONType) varRefType).constraint;
                if (constraintType.tag == TypeTags.STRUCT) {
                    //TODO Test this condition
                    BSymbol jsonFieldSymbol = symResolver.resolveStructField(fieldAccessExpr.pos, this.env,
                            fieldName, varRefType.tsymbol);
                    isTainted = jsonFieldSymbol.tainted;
                } else {
                    isTainted = fieldAccessExpr.expr.symbol.tainted;
                }
                break;
            case TypeTags.ENUM:
                isTainted = false;
                break;
        }
        setTaintedStatusList(isTainted);
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.expr.accept(this);
    }

    public void visit(BLangInvocation invocationExpr) {
        BLangInvokableNode invokableNode = null;
        if (invocationExpr.functionPointerInvocation) {
            invokableNode = ((BVarSymbol) invocationExpr.symbol).node;
            if (invokableNode == null) {
                // Skipping function pointers
                List<Boolean> returnTaintedStatus = new ArrayList<>();
                ((BInvokableType) invocationExpr.symbol.type).retTypes
                        .forEach(retType -> returnTaintedStatus.add(false));
                taintedStatus = returnTaintedStatus;
                return;
            }
        } else if (invocationExpr.iterableOperationInvocation) {
            //TODO: Skipping iterable invocations
            List<Boolean> returnTaintedStatus = new ArrayList<>();
            ((BInvokableType) invocationExpr.symbol.type).retTypes.forEach(retType -> returnTaintedStatus.add(false));
            taintedStatus = returnTaintedStatus;
            return;
        } else {
            invokableNode = (BLangInvokableNode) ((BInvokableSymbol) invocationExpr.symbol).node;
        }
        if (invokableNode.symbol.taintTable == null) {
           // if (!initialGenerationComplete) {
                BlockingNode blockingNode = new BlockingNode(invocationExpr.symbol.pkgID, invocationExpr.symbol.name);
                this.blockedNode = new BlockedNode(this.currPkgSymbol, null, invocationExpr.pos, blockingNode);
//                List<BlockedNode> nodeList = blockedInvokables.get(blockingNode);
//                if (nodeList == null) {
//                    nodeList = new ArrayList<>();
//                }
//                nodeList.add(blockedNode);

                //bloc.put(blockingNode, nodeList);
                //blockedInvokableCount++;
                stopAnalysis = true;
           // } else {
                // This is an indication for invokable to skip any further analysis.
                // TODO: Improve not to need a dummy object.
          //      this.blockedNode = new BlockedNode(this.currPkgSymbol, null, null, null);
          //      stopAnalysis = true;
           // }
            //Return a dummy list to make sure following statements do not break.
            List<Boolean> returnTaintedStatus = new ArrayList<>();
            invokableNode.retParams.forEach(param -> returnTaintedStatus.add(false));
            taintedStatus = returnTaintedStatus;
        } else {
            Map<Integer, List<Boolean>> taintTable = invokableNode.symbol.taintTable;
            List<Boolean> returnTaintedStatus = taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);
            if (invokableNode.params.size() == 0 && invokableNode.retParams.size() > 0) {
                for (BLangVariable retParam : invokableNode.retParams) {
                    returnTaintedStatus.add(retParam.flagSet.contains(Flag.TAINTED));
                }
            } else {
                for (int i = 0; i < invocationExpr.argExprs.size(); i++) {
                    invocationExpr.argExprs.get(i).accept(this);
                    // If current argument is tainted, look-up the taint-table for the record of
                    // return-tainted-status when the given argument is in tainted state.
                    if (taintedStatus.get(0)) {
                        List<Boolean> taintedStatusRecord = taintTable.get(i);
                        // This is null when current parameter is sensitive. Therefore, providing a tainted
                        // value to a sensitive parameter is invalid and should return a compiler error.
                        if (taintedStatusRecord == null) {
                            returnTaintedStatus = taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);
                            if (entrypointAnalysis) {
                                this.dlog.error(invocationExpr.pos, DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE,
                                        invokableNode.params.get(i).name.value);
                            } else {
                                taintError = new TaintError(invocationExpr.pos, invokableNode.params.get(i).name.value);
                                returnTaintedStatus = taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);
                                stopAnalysis = true;
                                break;
                            }
                        } else {
                            // Go through tainted status of each return. if return is tainted, update the
                            // returnTaintedStatus, so that it will have the accumulated tainted status of
                            // all returns.
                            for (int j = 0; j < taintedStatusRecord.size(); j++) {
                                boolean taintedStatus = taintedStatusRecord.get(j);
                                if (taintedStatus) {
                                    returnTaintedStatus.set(j, taintedStatus);
                                }
                            }
                        }
                    }
                }
            }
            taintedStatus = returnTaintedStatus;
        }

    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.thenExpr.accept(this);
        boolean thenTaintedCheckResult = taintedStatus.get(0);

        nonOverridingContext = true;
        ternaryExpr.elseExpr.accept(this);
        nonOverridingContext = false;

        boolean elseTaintedCheckResult = taintedStatus.get(0);
        setTaintedStatusList(thenTaintedCheckResult || elseTaintedCheckResult);
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr.accept(this);
        boolean lhsTaintedCheckResult = taintedStatus.get(0);
        binaryExpr.rhsExpr.accept(this);
        boolean rhsTaintedCheckResult = taintedStatus.get(0);
        setTaintedStatusList(lhsTaintedCheckResult || rhsTaintedCheckResult);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
            setTaintedStatusList(false);
        } else {
            unaryExpr.expr.accept(this);
        }
    }

    public void visit(BLangTypeofExpr accessExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangTypeCastExpr castExpr) {
        castExpr.expr.accept(this);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr.accept(this);
    }

    public void visit(BLangXMLQName xmlQName) {
        setTaintedStatusList(false);
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        //TODO check if symbol should be set here
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(xmlAttribute, env);

        // check attribute name
        xmlAttribute.name.accept(this);
        boolean attrNameTainedStatus = taintedStatus.get(0);

        // check attribute value
        xmlAttribute.value.accept(this);
        boolean attrValueTainedStatus = taintedStatus.get(0);

        setTaintedStatusList(attrNameTainedStatus || attrValueTainedStatus);
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(xmlElementLiteral, env);

        // Visit in-line namespace declarations
        boolean inLineNamespaceTainted = false;
        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && ((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                BLangExpression bLangExpression = attribute;
                bLangExpression.accept(this);
                attribute.symbol.tainted = taintedStatus.get(0);
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
                BLangExpression bLangExpression = attribute;
                bLangExpression.accept(this);
                attribute.symbol.tainted = taintedStatus.get(0);
                if (attribute.symbol.tainted) {
                    attributesTainted = true;
                }
            }
        }

        Map<Name, BXMLNSSymbol> namespaces = symResolver.resolveAllNamespaces(xmlElementEnv);
        Name defaultNs = names.fromString(XMLConstants.DEFAULT_NS_PREFIX);
        if (namespaces.containsKey(defaultNs)) {
            xmlElementLiteral.defaultNsSymbol = namespaces.remove(defaultNs);
        }
        xmlElementLiteral.namespacesInScope.putAll(namespaces);

        // Visit the tag names
        BLangExpression startTagName = xmlElementLiteral.startTagName;
        startTagName.accept(this);
        boolean startTagTaintedCheckResult = taintedStatus.get(0);
        BLangExpression endTagName = xmlElementLiteral.endTagName;
        boolean endTagTaintedCheckResult = false;
        if (endTagName != null) {
            endTagName.accept(this);
            endTagTaintedCheckResult = taintedStatus.get(0);
        }
        boolean tagNamesTainted = startTagTaintedCheckResult || endTagTaintedCheckResult;

        // Visit the children
        boolean childrenTainted = false;
        for (BLangExpression expr : xmlElementLiteral.children) {
            expr.accept(this);
            boolean childTaintedCheckResult = taintedStatus.get(0);
            if (childTaintedCheckResult) {
                childrenTainted = true;
            }
        }

        setTaintedStatusList(inLineNamespaceTainted || attributesTainted || tagNamesTainted || childrenTainted);
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        analyzeStringTemplateConcatExpr(xmlTextLiteral.textFragments);
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        analyzeStringTemplateConcatExpr(xmlCommentLiteral.textFragments);
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target.accept(this);
        if (!taintedStatus.get(0)) {
            analyzeStringTemplateConcatExpr(xmlProcInsLiteral.dataFragments);
        }
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        analyzeStringTemplateConcatExpr(xmlQuotedString.textFragments);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        analyzeStringTemplateConcatExpr(stringTemplateLiteral.exprs);
    }

    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.exprs.forEach(expr -> expr.accept(this));
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.exprs.forEach(expr -> expr.accept(this));
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        /* ignore */
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        //TODO
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        setTaintedStatusList(false);
    }

    // Type nodes

    public void visit(BLangValueType valueType) {
        /* ignore */
    }

    public void visit(BLangArrayType arrayType) {
        /* ignore */
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        /* ignore */
    }

    public void visit(BLangEndpointTypeNode endpointType) {
        /* ignore */
    }

    public void visit(BLangConstrainedType constrainedType) {
        /* ignore */
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        /* ignore */
    }

    public void visit(BLangFunctionTypeNode functionTypeNode) {
        /* ignore */
    }


    // expressions that will used only from the Desugar phase

    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        /* ignore */
    }

    public void visit(BLangFieldBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        /* ignore */
    }

    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        /* ignore */
    }

    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        /* ignore */
    }

    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        /* ignore */
    }

    public void visit(BLangRecordLiteral.BLangTableLiteral tableLiteral) {
        /* ignore */
    }

    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        /* ignore */
    }

    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        /* ignore */
    }

    public void visit(BLangInvocation.BLangTransformerInvocation iExpr) {
        /* ignore */
    }

    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        /* ignore */
    }

    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        /* ignore */
    }

    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        /* ignore */
    }

    public void visit(BLangFieldBasedAccess.BLangEnumeratorAccessExpr enumeratorAccessExpr) {
        /* ignore */
    }

    // Private

    private <T extends BLangExpression, U extends SymbolEnv> void analyzeExpr(T t, U u) {
        analyzeNode(t, u);
    }

    private <T extends BLangNode, U extends SymbolEnv> void analyzeNode(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
    }

    private void analyzeStringTemplateConcatExpr(List<BLangExpression> exprs) {
        for (BLangExpression expr : exprs) {
            expr.accept(this);
            if (taintedStatus.get(0)) {
                break;
            }
        }
    }

    private void setTaintedStatus(BLangVariable varNode, boolean taintedCheckResult) {
        // In an non overriding context (such as else condition of a if statement,
        // it should not be allowed to change the tainted status from true to false
        // this prevents identifying variable "example" as untainted.
        // var example = "test"; if (condition) { example = "untainted"; } else { example = taintedValue; }
        if (!nonOverridingContext) {
            varNode.symbol.tainted = taintedCheckResult;
        } else if (nonOverridingContext && !varNode.symbol.tainted) {
            varNode.symbol.tainted = taintedCheckResult;
        }
    }

    private void setTaintedStatus(BLangVariableReference varNode, boolean taintedCheckResult) {
        // In an non overriding context (such as else condition of a if statement,
        // it should not be allowed to change the tainted status from true to false
        // this prevents identifying variable "example" as untainted.
        // var example = "test"; if (condition) { example = "untainted"; } else { example = taintedValue; }
        if (!nonOverridingContext) {
            varNode.symbol.tainted = taintedCheckResult;
        } else if (nonOverridingContext && !varNode.symbol.tainted) {
            varNode.symbol.tainted = taintedCheckResult;
        }
    }

    private void setTaintedStatusList(boolean taintedStatus) {
        List<Boolean> taintedStatusList = new ArrayList<>();
        taintedStatusList.add(taintedStatus);
        this.taintedStatus = taintedStatusList;
    }

    private void attachTaintTableBasedOnFlags(BLangInvokableNode invNode) {
        if (invNode.symbol.taintTable == null) {
            Map<Integer, List<Boolean>> taintTable = new HashMap<>();
            // Extract tainted status of native function from the return modifier.
            List<Boolean> retParamsTaintedStatus = new ArrayList<>();
            for (BLangVariable retParam : invNode.retParams) {
                retParamsTaintedStatus.add(retParam.flagSet.contains(Flag.TAINTED));
            }

            // Add tainted status when no parameter is tainted.
            List<BLangVariable> params = invNode.params;
            taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, retParamsTaintedStatus);

            if (invNode.params.size() > 0) {
                // Add taubred status when each parameter is tainted.
                for (int i = 0; i < params.size(); i++) {
                    // If parameter is sensitive, it is invalid to have a case
                    // where tainted status of parameter is true.
                    if (params.get(i).flagSet.contains(Flag.SENSITIVE)) {
                        continue;
                    }
                    taintTable.put(i, retParamsTaintedStatus);
                }
            }
            invNode.symbol.taintTable = taintTable;
        }
    }

    private void resolveBlockedInvokables() {
        while (blockedNodes.size() > 0) {
            int remainingBlockedInvokableCount = 0;
            //Map<BlockingNode, List<BlockedNode>> remainingBlockedInvokables = new HashMap<>();
            //for (BlockingNode blockingNode : blockedInvokables.keySet()) {
                Set<BlockedNode> remainingBlockedNodes = new HashSet<>();
                for (BlockedNode blockedNode : blockedNodes) {
                    this.env = blockedNode.pkgSymbol;
                    blockedNode.invokableNode.accept(this);
                    if (blockedNode.invokableNode.symbol.taintTable == null) {
                        remainingBlockedNodes.add(blockedNode);
                    }
                }
            //}
            if (blockedNodes.size() == remainingBlockedNodes.size()) {
                // No block has been resolved. There is a loop of dependencies.
                //for (BlockingNode blockingNode : remainingBlockedNodes) {
                 //   List<BlockedNode> blockedNodes = remainingBlockedInvokables.get(blockingNode);
                    for (BlockedNode blockedNode : blockedNodes) {
                        attachTaintTableBasedOnFlags(blockedNode.invokableNode);
                        this.dlog.warning(blockedNode.blockedPos,
                                DiagnosticCode.UNABLE_TO_PERFORM_TAINT_CHECKING_IN_LOOP,
                                blockedNode.invokableNode.name.value);
                    }
                //}
                break;
            }
            blockedNodes = remainingBlockedNodes;
        }
    }

    private void visitEntrypoint(BLangInvokableNode invNode, SymbolEnv funcEnv) {
        entrypointAnalysis = true;
        if (invNode.params != null) {
            invNode.params.forEach(param -> param.symbol.tainted = true);
        }
        analyzeReturnTaintedStatus(invNode, funcEnv);
        if (this.blockedNode != null) {
            // Set the function being analyzed to the map and clean the tree for next invocation.
            this.blockedNode.invokableNode = invNode;
            this.blockedNode = null;
            return;
        }
        invNode.symbol.taintTable = new HashMap<>();
        invNode.params.forEach(param -> param.symbol.tainted = false);
        entrypointAnalysis = false;
    }

    private boolean isEntryPoint(BLangFunction funcNode) {
        return isMainFunction(funcNode) || isServiceResource(funcNode);
    }

    private boolean isMainFunction(BLangFunction funcNode) {
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

    private boolean isServiceResource(BLangFunction funcNode) {
        funcNode = funcNode;
        return false;
    }

    private void visitInvocable(BLangInvokableNode invNode, SymbolEnv symbolEnv) {
        if (invNode.symbol.taintTable == null) {
            if (Symbols.isNative(invNode.symbol)) {
                attachTaintTableBasedOnFlags(invNode);
                return;
            }
            Map<Integer, List<Boolean>> taintTable = new HashMap<>();
            // Check the tainted status of return values when no parameter is tainted
            analyzeReturnTaintedStatus(invNode, null, symbolEnv);
            // If the table generation cannot be done because a depending function has not been analyzed
            if (this.blockedNode != null) {
                // Set the function being analyzed to the map and clean the tree for next invocation.
                this.blockedNode.invokableNode = invNode;
                if (!initialGenerationComplete) {
                    blockedNodes.add(blockedNode);

//                    this.dlog.error(blockedNode.invokableNode.pos, DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE,
//                            blockedNode.invokableNode.symbol.pkgID
//                                    + "/" + blockedNode.invokableNode.symbol.name + " < BLOCKED ON > " +
//                                    blockedNode.blockingNode.packageID
//                        + "/" + blockedNode.blockingNode.name);
                }
                this.blockedNode = null;
                return;
            }

//            this.dlog.error(invNode.pos, DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE,
//                    " ANALYZED > " + invNode.symbol.pkgID + "/" + invNode.symbol.name);

            updateBasedOnFlags(taintedStatus, invNode.retParams);
            taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, taintedStatus);
            // Compiler error if a return is always tainted, but it has not been marked 'tainted'.
            if (invNode.retParams != null && invNode.retParams.size() > 0) {
                for (int i = 0; i < taintedStatus.size(); i++) {
                    BLangVariable retParam = invNode.retParams.get(i);
                    if (taintedStatus.get(i) && !retParam.flagSet.contains(Flag.TAINTED)) {
                        this.dlog.error(invNode.pos, DiagnosticCode.RETURN_MUST_BE_TAINTED, retParam.name.value);
                        return;
                    }
                }
            }
            List<BLangVariable> params = invNode.params;
            for (int i = 0; i < params.size(); i++) {
                BLangVariable param = params.get(i);
                // If parameter is sensitive, it is invalid to have a case
                // where tainted status of parameter is true.
                if (param.flagSet.contains(Flag.SENSITIVE)) {
                    continue;
                }
                // Set new parameter state, analyze the body and see what is the outcome of the function.
                analyzeReturnTaintedStatus(invNode, param, symbolEnv);
                if (taintError != null) {
                    // When invocation returns an error, due to passing a tainted argument to a sensitive parameter
                    // skip adding the current invalid combination to the table. This will infer the sensitive
                    // state of invocations, into owner.
                    taintError = null;
                } else {
                    updateBasedOnFlags(taintedStatus, invNode.retParams);
                    taintTable.put(i, taintedStatus);
                }
            }
            //Clean-up old state.
            invNode.params.forEach(param -> param.symbol.tainted = false);
            invNode.symbol.taintTable = taintTable;
        }
    }

    private void updateBasedOnFlags(List<Boolean> taintedStatus, List<BLangVariable> retParams) {
        for (int i = 0; i < retParams.size(); i++) {
            BLangVariable param = retParams.get(i);
            boolean observedReturnTaintedStatus = taintedStatus.get(i);
            if (!observedReturnTaintedStatus) {
                taintedStatus.set(i, param.flagSet.contains(Flag.TAINTED));
            }
        }
    }

    private void analyzeReturnTaintedStatus(BLangInvokableNode invNode, BLangVariable testParam, SymbolEnv symbolEnv) {
        //Clean-up old state
        if (invNode.params != null) {
            invNode.params.forEach(param -> param.symbol.tainted = false);
        }
        // Mark given parameter as tainted
        if (testParam != null) {
            invNode.params.get(invNode.params.indexOf(testParam)).symbol.tainted = true;
        }
        analyzeReturnTaintedStatus(invNode, symbolEnv);
    }

    private void analyzeReturnTaintedStatus(BLangInvokableNode invNode, SymbolEnv symbolEnv) {
        // Iteration to see the tainted status of returns when no input parameter is tainted
        if (invNode.workers.isEmpty()) {
            analyzeNode(invNode.body, symbolEnv);
        } else {
            for (BLangWorker worker : invNode.workers) {
                analyzeNode(worker, symbolEnv);
                if (this.blockedNode != null || taintError != null) {
                    break;
                }
            }
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

    private class TaintError {
        public DiagnosticPos pos;
        public String paramName;

        public TaintError(DiagnosticPos pos, String paramName) {
            this.pos = pos;
            this.paramName = paramName;
        }
    }
}
