package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;

/**
 * Responsible of following operations related to taint checking:
 *
 * (*) Performing data flow analysis and propagating tainted status.
 * (*) Validating if invocations use tainted values with secure parameters
 */
public class TaintAnalyzer extends BLangNodeVisitor {
    private static final CompilerContext.Key<TaintAnalyzer> TAINT_ANALYZER_KEY =
            new CompilerContext.Key<>();

    //TODO: Check the possibility of getting this from CodeGenerator (currently private)
    private static final String MAIN_FUNCTION_NAME = "main";
    private static final String SECURE_ANNOTATION_NAME = "secure";

    private BLangPackage pkgNode;
    private Names names;
    private SymbolResolver symResolver;
    private SymbolEnter symbolEnter;
    private DiagnosticLog dlog;
    private SymbolEnv env;

    private List<Boolean> taintedStatus;
    private boolean nonOverridingContext;
    private BLangInvocation invocation;

    private Map<FunctionIdentifier, BLangFunction> lambdaFunctions = new HashMap<FunctionIdentifier, BLangFunction>();

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
        this.pkgNode = pkgNode;
        pkgNode.accept(this);
        return pkgNode;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TAINT_ANALYZE)) {
            return;
        }
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        pkgNode.functions.stream().filter(f -> (f.name.value.equals(MAIN_FUNCTION_NAME) && f.symbol.params.size() == 1
                && f.symbol.retParams.size() == 0)).forEach(function ->  analyzeInitFunction(function, pkgEnv));
        pkgNode.services.forEach(service -> analyzeNode(service, pkgEnv));
        pkgNode.completedPhases.add(CompilerPhase.TAINT_ANALYZE);
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }
        analyzeNode(pkgEnv.node, pkgEnv);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        analyzeNode(xmlnsNode.namespaceURI, env);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        funcNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint = new BLangAnnotationAttachmentPoint(
                    BLangAnnotationAttachmentPoint.AttachmentPoint.FUNCTION, null);
            analyzeNode(annotationAttachment, funcEnv);
        });

        if (invocation != null && funcNode.params != null) {
            validateParameters(funcNode.params, invocation.pos,
                    DiagnosticCode.CANNOT_USE_TAINTED_VALUE_WITH_SECURE_PARAMETER);
        }

        if (funcNode.body != null) {
            analyzeNode(funcNode.body, env);
        } else {
            // This will capture native implementations. Anything returned by a native function is considered tainted.
            List taintedCheckStatus = new ArrayList();
            funcNode.retParams.forEach(param -> taintedCheckStatus.add(true));
            taintedStatus = taintedCheckStatus;
        }
        this.processWorkers(funcNode, funcEnv);
    }

    private void validateParameters(List<BLangVariable> params, DiagnosticPos diagnosticPos,
                                    DiagnosticCode diagnosticCode) {
        params.forEach(param -> {
            if (param.getAnnotationAttachments() != null) {
                param.getAnnotationAttachments().forEach(annotationAttachment -> {
                    if (annotationAttachment.getAnnotationName().getValue().equals(SECURE_ANNOTATION_NAME)) {
                        if (param.symbol.tainted) {
                            this.dlog.error(diagnosticPos, diagnosticCode, param.name.value);
                        }
                    }
                });
            }
        });
    }
    public void visit(BLangStruct structNode) {
        structNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint = new BLangAnnotationAttachmentPoint(
                    BLangAnnotationAttachmentPoint.AttachmentPoint.STRUCT, null);
            annotationAttachment.accept(this);
        });
    }

    @Override
    public void visit(BLangEnum enumNode) {
        enumNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint = new BLangAnnotationAttachmentPoint(
                    BLangAnnotationAttachmentPoint.AttachmentPoint.ENUM, null);
            annotationAttachment.accept(this);
        });
    }

    public void visit(BLangAnnotation annotationNode) {
        SymbolEnv annotationEnv = SymbolEnv.createAnnotationEnv(annotationNode, annotationNode.symbol.scope, env);
        annotationNode.attributes.forEach(attribute -> {
            analyzeNode(attribute, annotationEnv);
        });
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        analyzeNode(annotationAttribute.expr, env);
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {

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
                List<Boolean> taintedCheckResults = analyzeNode(varNode.expr, varInitEnv);
                if (varNode.expr instanceof BLangLambdaFunction) {
                    BLangFunction function = ((BLangLambdaFunction) varNode.expr).function;
                    lambdaFunctions.put(new FunctionIdentifier(varNode.symbol.pkgID, varNode.getName()), function);
                } else {
                    setTaintedStatus(varNode, taintedCheckResults.get(0));
                }
            }
        }
    }

    // Statements

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, this.env);
        blockNode.stmts.forEach(stmt -> analyzeNode(stmt, blockEnv));
    }

    public void visit(BLangVariableDef varDefNode) {
        analyzeNode(varDefNode.var, env);
    }

    public void visit(BLangAssignment assignNode) {
        List<Boolean> taintedCheckResults = analyzeNode(assignNode.expr, this.env);

        // This becomes false when analyzing function-invocations that are out of compilation scope (built-ins,
        // external connectors)
        boolean multiReturnHandledProperly = assignNode.varRefs != null && taintedCheckResults.size() ==
                assignNode.varRefs.size();
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression expr = assignNode.varRefs.get(i);
            if (expr instanceof BLangVariableReference) {
                if (multiReturnHandledProperly) {
                    setTaintedStatus((BLangVariableReference) expr, taintedCheckResults.get(i));
                } else {
                    setTaintedStatus((BLangVariableReference) expr, taintedCheckResults.get(0));
                }
            }
        }
    }

    public void visit(BLangBind bindNode) {
        BLangExpression varRef = bindNode.varRef;
        boolean taintedCheckResult = analyzeNode(bindNode.expr, this.env).get(0);
        if (varRef instanceof BLangVariableReference) {
            setTaintedStatus((BLangVariableReference) varRef, taintedCheckResult);
        }
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        // Creates a new environment here.
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        analyzeNode(exprStmtNode.expr, stmtEnv);
    }

    public void visit(BLangIf ifNode) {
        analyzeNode(ifNode.body, env);

        if (ifNode.elseStmt != null) {
            analyzeNonOverridingNode(ifNode.elseStmt, env);
        }
    }

    public void visit(BLangForeach foreach) {
        analyzeNode(foreach.collection, env);
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        analyzeNode(foreach.body, blockEnv);
    }

    public void visit(BLangWhile whileNode) {
        analyzeNode(whileNode.expr, env);
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(whileNode.body, env);
        analyzeNode(whileNode.body, blockEnv);
    }

    public void visit(BLangLock lockNode) {
        analyzeNode(lockNode.body, env);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        connectorNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.CONNECTOR,
                            null);
            analyzeNode(a, connectorEnv);
        });
        connectorNode.params.forEach(param -> analyzeNode(param, connectorEnv));
        connectorNode.varDefs.forEach(varDef -> analyzeNode(varDef, connectorEnv));

        analyzeNode(connectorNode.initFunction, connectorEnv);
        connectorNode.actions.forEach(action -> this.analyzeNode(action, connectorEnv));
        analyzeNode(connectorNode.initAction, connectorEnv);
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;

        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        actionNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ACTION,
                            null);
            analyzeNode(a, actionEnv);
        });

        if (invocation != null && actionNode.params != null) {
            validateParameters(actionNode.params, invocation.pos,
                    DiagnosticCode.CANNOT_USE_TAINTED_VALUE_WITH_SECURE_PARAMETER);
        }
        if (Symbols.isNative(actionSymbol)) {
            return;
        }
        analyzeNode(actionNode.body, actionEnv);
        analyzeNode(actionNode, actionEnv);
    }

    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, env);
        BSymbol protocolPkg = symResolver.resolvePkgSymbol(serviceNode.pos,
                serviceEnv, names.fromIdNode(serviceNode.protocolPkgIdentifier));
        //TODO validate protocol package existance
        ((BTypeSymbol) serviceSymbol).protocolPkgId = protocolPkg.pkgID;
        serviceNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.SERVICE,
                            protocolPkg.pkgID.name.getValue());
            analyzeNode(a, serviceEnv);
        });
        serviceNode.vars.forEach(v -> analyzeNode(v, serviceEnv));
        analyzeNode(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(r -> analyzeNode(r, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        resourceNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.RESOURCE,
                            null);
            analyzeNode(a, resourceEnv);
        });

        resourceNode.params.forEach(param -> param.symbol.tainted = true);
        analyzeNode(resourceNode.body, resourceEnv);
        this.processWorkers(resourceNode, resourceEnv);
    }

    public void visit(BLangTryCatchFinally tryCatchFinally) {
        analyzeNode(tryCatchFinally.tryBody, env);
        tryCatchFinally.catchBlocks.forEach(c -> analyzeNonOverridingNode(c, env));
        if (tryCatchFinally.finallyBody != null) {
            analyzeNonOverridingNode(tryCatchFinally.finallyBody, env);
        }
    }

    public void visit(BLangCatch bLangCatch) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, env);
        analyzeNode(bLangCatch.param, catchBlockEnv);
        analyzeNode(bLangCatch.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeNode(transactionNode.transactionBody, env);
        if (transactionNode.failedBody != null) {
            analyzeNonOverridingNode(transactionNode.failedBody, env);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // Do nothing
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        //TODO
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        analyzeNode(workerNode.body, workerEnv);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.exprs.forEach(e -> analyzeNode(e, this.env));
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.exprs.forEach(e -> analyzeNode(e, this.env));
    }

    @Override
    public void visit(BLangReturn returnNode) {
        //TODO Named returns
        List<Boolean> taintedCheckResults = new ArrayList<>();
        returnNode.exprs.forEach(expr -> taintedCheckResults.add(analyzeNode(expr, env).get(0)));
        taintedStatus = taintedCheckResults;
    }


    public void visit(BLangNext nextNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, env);
        transformerNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint = new BLangAnnotationAttachmentPoint(
                    BLangAnnotationAttachmentPoint.AttachmentPoint.TRANSFORMER, null);
            analyzeNode(annotationAttachment, transformerEnv);
        });

        analyzeNode(transformerNode.body, transformerEnv);

        this.processWorkers(transformerNode, transformerEnv);
    }

    // Expressions
    public void visit(BLangLiteral literalExpr) {
        taintedStatus = builtTaintedStatusList(false);
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        taintedStatus = builtTaintedStatusList(arrayLiteral.exprs.stream().anyMatch(expr ->
                analyzeNode(expr, this.env).get(0)));
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        boolean isTainted = false;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            boolean taintedCheckResult = analyzeNode(keyValuePair.valueExpr, this.env).get(0);

            // Make sure the symbol for key value pair is updated with the final decision if the pair is in symbol table
            if (keyValuePair.key.fieldSymbol != null) {
                keyValuePair.key.fieldSymbol.tainted = taintedCheckResult;
            }
            // To update the tainted status of the variable
            if (taintedCheckResult) {
                isTainted = true;
            }
        }
        taintedStatus = builtTaintedStatusList(isTainted);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.symbol == null) {
            Name varName = names.fromIdNode(varRefExpr.variableName);
            if (varName != Names.IGNORE) {
                BSymbol symbol = symResolver.lookupSymbolInPackage(varRefExpr.pos, env,
                        names.fromIdNode(varRefExpr.pkgAlias), varName, SymTag.VARIABLE_NAME);
                taintedStatus = builtTaintedStatusList(symbol.tainted);
            }
        } else {
            taintedStatus = builtTaintedStatusList(varRefExpr.symbol.tainted);
        }
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BType varRefType = fieldAccessExpr.expr.type;
        Boolean isTainted = true;
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

        taintedStatus = builtTaintedStatusList(isTainted);
    }

    public void visit(BLangIndexBasedAccess indexBasedAccessExpr) {
        boolean taintedCheckResult = analyzeNode(indexBasedAccessExpr.expr, this.env).get(0);
        taintedStatus = builtTaintedStatusList(taintedCheckResult);
    }

    public void visit(BLangInvocation iExpr) {
        BLangInvocation prevInvocationContext = this.invocation;
        this.invocation = iExpr;

        SymbolEnv pkgEnv = null;
        for (BPackageSymbol packageSymbol : symbolEnter.packageEnvs.keySet()) {
            if (packageSymbol.pkgID.equals(iExpr.symbol.pkgID)) {
                pkgEnv = symbolEnter.packageEnvs.get(packageSymbol);
                break;
            }
        }

        List<BLangInvokableNode> invokableNodeList = new ArrayList<>();
        if (iExpr.functionPointerInvocation) {
            invokableNodeList.add(lambdaFunctions.get(new FunctionIdentifier(iExpr.symbol.pkgID, iExpr.name)));
        } else if (iExpr.symbol.kind.equals(SymbolKind.FUNCTION)) {
            invokableNodeList = ((BLangPackage) pkgEnv.node).functions.stream().filter(f -> (f.name.equals(iExpr.name)))
                    .collect(Collectors.toList());
        } else if (iExpr.symbol.kind.equals(SymbolKind.ACTION)) {
            //TODO Needs improvement (when multiple connectors have same action name)
            List<BLangConnector> connectorList = ((BLangPackage) pkgEnv.node).connectors.stream().filter(f ->
                    f.symbol.name.equals(iExpr.symbol.owner.name)).collect(Collectors.toList());
            for (BLangConnector connector : connectorList) {
                invokableNodeList = connector.getActions().stream().filter(f -> f.name.equals(iExpr.name))
                        .collect(Collectors.toList());
                if (invokableNodeList.size() > 0) {
                    break;
                }
            }
        }

        if (pkgEnv != null && invokableNodeList.size() > 0) {
            // Transfer tainted status of arguments to function parameters
            for (int i = 0; i < iExpr.argExprs.size(); i++) {
                boolean taintedCheckResult = analyzeNode(iExpr.argExprs.get(i), env).get(0);
                setTaintedStatus(invokableNodeList.get(0).params.get(i), taintedCheckResult);
            }
            for (BLangInvokableNode function : invokableNodeList) {
                analyzeNode(function, pkgEnv);
            }
        } else {
            taintedStatus = builtTaintedStatusList(true);
        }

        invocation = prevInvocationContext;
    }

    public void visit(BLangConnectorInit cIExpr) {
        SymbolEnv pkgEnv = null;

        List<BLangConnector> connectors = pkgNode.connectors.stream().filter(f -> (f.name.value
                .equals(cIExpr.connectorType.typeName.value)))
                .collect(Collectors.toList());

        if (connectors.size() == 0) {
            if (cIExpr.type.tsymbol != null && cIExpr.type.tsymbol.owner != null && cIExpr.type.tsymbol.owner
                    instanceof BPackageSymbol) {
                BPackageSymbol pkgSymbol = (BPackageSymbol) cIExpr.type.tsymbol.owner;
                //TODO: Need to check why hashcodes are not matching
                for (BPackageSymbol packageSymbol : symbolEnter.packageEnvs.keySet()) {
                    if (packageSymbol.equals(pkgSymbol)) {
                        pkgEnv = symbolEnter.packageEnvs.get(packageSymbol);
                        break;
                    }
                }
                if (pkgEnv.node instanceof BLangPackage) {
                    connectors = ((BLangPackage) pkgEnv.node).connectors.stream().filter(f -> (f.name.value
                            .equals(cIExpr.connectorType.typeName.value)))
                            .collect(Collectors.toList());
                }
            }
        } else {
            pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        }

        if (connectors.size() > 0) {
            BLangConnector connector = connectors.get(0);
            if (cIExpr.argsExpr != null) {
                // Transfer tainted status if arguments to parameters of the connector-init function
                for (int i = 0; i < cIExpr.argsExpr.size(); i++) {
                    boolean taintedStatusCheck = analyzeNode(cIExpr.argsExpr.get(i), this.env).get(0);
                    connector.params.get(i).symbol.tainted = taintedStatusCheck;
                }
            }
            validateParameters(connector.params, cIExpr.pos,
                    DiagnosticCode.CANNOT_USE_TAINTED_VALUE_WITH_SECURE_PARAMETER);
        }
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        boolean thenTaintedCheckResult = analyzeNode(ternaryExpr.thenExpr, env).get(0);
        boolean elseTaintedCheckResult = analyzeNode(ternaryExpr.elseExpr, env).get(0);
        taintedStatus = builtTaintedStatusList(thenTaintedCheckResult || elseTaintedCheckResult);
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        boolean lhsTaintedCheckResult = analyzeNode(binaryExpr.lhsExpr, env).get(0);
        boolean rhsTaintedCheckResult = analyzeNode(binaryExpr.rhsExpr, env).get(0);
        taintedStatus = builtTaintedStatusList(lhsTaintedCheckResult || rhsTaintedCheckResult);
    }

    public void visit(BLangTypeofExpr accessExpr) {
        taintedStatus = builtTaintedStatusList(false);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
            taintedStatus = builtTaintedStatusList(false);
        } else {
            boolean taintedCheckResult = analyzeNode(unaryExpr.expr, env).get(0);
            taintedStatus = builtTaintedStatusList(taintedCheckResult);
        }
    }

    public void visit(BLangTypeCastExpr castExpr) {
        boolean taintedCheckResult = analyzeNode(castExpr.expr, env).get(0);
        taintedStatus = builtTaintedStatusList(taintedCheckResult);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        boolean taintedCheckResult = analyzeNode(conversionExpr.expr, env).get(0);
        taintedStatus = builtTaintedStatusList(taintedCheckResult);
    }


    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        //lambdaFunctions.add(bLangLambdaFunction);
//        if (invocation != null) {
//            List<Boolean> taintedCheckResults = new ArrayList<>();
//            if (bLangLambdaFunction.function.retParams != null) {
//                for (BLangVariable returnVar : bLangLambdaFunction.function.retParams) {
//                    taintedCheckResults.add(returnVar.symbol.tainted);
//                }
//            }
//            taintedStatus = taintedCheckResults;
//        }
    }

    public void visit(BLangXMLQName bLangXMLQName) {
        taintedStatus = builtTaintedStatusList(false);
    }

    public void visit(BLangXMLAttribute bLangXMLAttribute) {
        //TODO check if symbol should be set here
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(bLangXMLAttribute, env);

        // check attribute name
        boolean attrNameTainedStatus = analyzeNode(bLangXMLAttribute.name, xmlAttributeEnv).get(0);

        // check attribute value
        boolean attrValueTainedStatus = analyzeNode(bLangXMLAttribute.value, xmlAttributeEnv).get(0);

        taintedStatus = builtTaintedStatusList(attrNameTainedStatus || attrValueTainedStatus);
    }

    public void visit(BLangXMLElementLiteral bLangXMLElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(bLangXMLElementLiteral, env);

        // Visit in-line namespace declarations
        boolean inLineNamespaceTainted = false;
        for (BLangXMLAttribute attribute : bLangXMLElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && ((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                BLangExpression bLangExpression = attribute;
                attribute.symbol.tainted = analyzeNode(bLangExpression, xmlElementEnv).get(0);
                if (attribute.symbol.tainted) {
                    inLineNamespaceTainted = true;
                }
            }
        }

        // Visit attributes.
        boolean attributesTainted = false;
        for (BLangXMLAttribute attribute : bLangXMLElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && !((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                BLangExpression bLangExpression = attribute;
                attribute.symbol.tainted = analyzeNode(bLangExpression, xmlElementEnv).get(0);
                if (attribute.symbol.tainted) {
                    attributesTainted = true;
                }
            }
        }

        Map<Name, BXMLNSSymbol> namespaces = symResolver.resolveAllNamespaces(xmlElementEnv);
        Name defaultNs = names.fromString(XMLConstants.DEFAULT_NS_PREFIX);
        if (namespaces.containsKey(defaultNs)) {
            bLangXMLElementLiteral.defaultNsSymbol = namespaces.remove(defaultNs);
        }
        bLangXMLElementLiteral.namespacesInScope.putAll(namespaces);

        // Visit the tag names
        BLangExpression startTagName = bLangXMLElementLiteral.startTagName;
        boolean startTagTaintedCheckResult = analyzeNode(startTagName, xmlElementEnv).get(0);
        BLangExpression endTagName = bLangXMLElementLiteral.endTagName;
        boolean endTagTaintedCheckResult = false;
        if (endTagName != null) {
            endTagTaintedCheckResult = analyzeNode(endTagName, xmlElementEnv).get(0);
        }
        boolean tagNamesTainted = startTagTaintedCheckResult || endTagTaintedCheckResult;

        // Visit the children
        boolean childrenTainted = false;
        for (BLangExpression expr : bLangXMLElementLiteral.children) {
            boolean childTaintedCheckResult = analyzeNode(expr, xmlElementEnv).get(0);
            if (childTaintedCheckResult) {
                childrenTainted = true;
            }
        }

        taintedStatus = builtTaintedStatusList(inLineNamespaceTainted || attributesTainted || tagNamesTainted ||
                childrenTainted);
    }

    public void visit(BLangXMLTextLiteral bLangXMLTextLiteral) {
        boolean taintedCheckResult = getStringTemplateConcatExprTaintedStatus(bLangXMLTextLiteral.textFragments);
        taintedStatus = builtTaintedStatusList(taintedCheckResult);
    }

    public void visit(BLangXMLCommentLiteral bLangXMLCommentLiteral) {
        boolean taintedCheckResult = getStringTemplateConcatExprTaintedStatus(bLangXMLCommentLiteral.textFragments);
        taintedStatus = builtTaintedStatusList(taintedCheckResult);
    }

    public void visit(BLangXMLProcInsLiteral bLangXMLProcInsLiteral) {
        boolean taintedCheckResult = analyzeNode((BLangExpression) bLangXMLProcInsLiteral.target, env).get(0);
        taintedCheckResult = taintedCheckResult ||
                getStringTemplateConcatExprTaintedStatus(bLangXMLProcInsLiteral.dataFragments);
        taintedStatus = builtTaintedStatusList(taintedCheckResult);
    }

    public void visit(BLangXMLQuotedString bLangXMLQuotedString) {
        boolean taintedCheckResult = getStringTemplateConcatExprTaintedStatus(bLangXMLQuotedString.textFragments);
        taintedStatus = builtTaintedStatusList(taintedCheckResult);
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        //TODO
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        boolean taintedCheckResult = getStringTemplateConcatExprTaintedStatus(stringTemplateLiteral.exprs);
        taintedStatus = builtTaintedStatusList(taintedCheckResult);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        boolean startTaintedCheckResult = analyzeNode(intRangeExpression.startExpr, env).get(0);
        boolean endTaintedCheckResult = analyzeNode(intRangeExpression.endExpr, env).get(0);

        taintedStatus = builtTaintedStatusList(startTaintedCheckResult || endTaintedCheckResult);
    }

    // Private methods
    private <T extends BLangNode, U extends SymbolEnv> List<Boolean> analyzeNode(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
        return taintedStatus;
    }

    private <T extends BLangNode, U extends SymbolEnv> List<Boolean> analyzeNonOverridingNode(T t, U u) {
        nonOverridingContext = true;
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
        nonOverridingContext = false;
        return taintedStatus;
    }

    private boolean getStringTemplateConcatExprTaintedStatus(List<BLangExpression> exprs) {
        boolean isTainted = false;
        for (BLangExpression expr : exprs) {
            List<Boolean> taintedCheckResult = analyzeNode(expr, env);
            if (taintedCheckResult.get(0)) {
                isTainted = true;
                break;
            }
        }
        return isTainted;
    }

    /**
     * This is taken from CodeGenerator (need a way of sharing this method between two analyzers).
     *
     * @param pkgNode
     * @return
     */
    private static BLangFunction getMainFunction(BLangPackage pkgNode) {
        List<BLangFunction> functions = pkgNode.functions.stream().filter(f -> (f.name.value
                .equals(MAIN_FUNCTION_NAME) && f.symbol.params.size() == 1 && f.symbol.retParams.size() == 0))
                .collect(Collectors.toList());
        if (functions.isEmpty()) {
            return null;
        }
        for (BLangFunction f : functions) {
            BType paramType = f.symbol.params.get(0).type;
            if (paramType.tag != TypeTags.ARRAY) {
                continue;
            }
            BArrayType arrayType = (BArrayType) paramType;
            if (arrayType.eType.tag == TypeTags.STRING) {
                return f;
            }
        }
        return null;
    }

    private void analyzeInitFunction(BLangFunction funcNode, SymbolEnv pkgEnv) {
        // Marking all input arguments tainted
        funcNode.params.forEach(param -> param.symbol.tainted = true);

        // Start analysing the body of the main function
        //function.body.accept(this);
        analyzeNode(funcNode, pkgEnv);
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

    private void processWorkers(BLangInvokableNode invNode, SymbolEnv invEnv) {
        if (invNode.workers.size() > 0) {
            invNode.workers.forEach(e -> analyzeNode(e, invEnv));
        }
    }

    private List<Boolean> builtTaintedStatusList(boolean taintedStatus) {
        List<Boolean> taintedStatusList = new ArrayList<>();
        taintedStatusList.add(taintedStatus);
        return taintedStatusList;
    }

    private class FunctionIdentifier {
        public PackageID packageId;
        public BLangIdentifier identifier;

        public FunctionIdentifier(PackageID packageId, BLangIdentifier identifier) {
            this.packageId = packageId;
            this.identifier = identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FunctionIdentifier that = (FunctionIdentifier) o;
            return packageId.equals(that.packageId) &&
                    identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            int result = packageId.hashCode();
            result = 31 * result + identifier.hashCode();
            return result;
        }
    }
}
