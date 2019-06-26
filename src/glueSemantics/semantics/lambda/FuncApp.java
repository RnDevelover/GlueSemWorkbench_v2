/*
 * Copyright 2018 Mark-Matthias Zymla & Moritz Messmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package glueSemantics.semantics.lambda;

import glueSemantics.semantics.FunctionalApplication;
import glueSemantics.semantics.MeaningRepresentation;
import glueSemantics.semantics.SemanticRepresentation;
import prover.LLProver;
import prover.ProverException;

import java.util.HashSet;
import java.util.Set;

import static main.Settings.PLAIN;
import static main.Settings.PROLOG;

public class FuncApp extends SemanticExpression implements FunctionalApplication {

    private SemanticRepresentation functor;
    private SemanticRepresentation argument;

    public FuncApp(SemanticRepresentation functor, SemanticRepresentation argument) {
        //this.functor = functor;
        //this.argument = argument;
        this.instantiateFunctionalApp(functor,argument);
    }

    public FuncApp(FuncApp fa) {
        this.functor = fa.functor.clone();
        this.argument = fa.argument.clone();

        //Test version
        //this.compiled = fa.compiled;
    }

    public SemanticRepresentation getFunctor() {
        return functor;
    }

    public SemanticRepresentation getArgument() {
        return argument;
    }



    // Does a full beta reduction of the term including all nested functional applications
    public SemanticRepresentation betaReduce() throws ProverException {
        if (argument != null)
            return apply(argument);
        return this;
    }

    /*
    Applies the functor to the argument. The functor must be a SemFunc or SemQuantEx and the
    applyTo() function of the functor is called for the actual application step.
    */
    public SemanticRepresentation apply(SemanticRepresentation arg) throws ProverException {
        if (this.functor instanceof SemFunction) {
            SemFunction lambda = (SemFunction) this.functor;
            if (lambda.getBinder().getType().equalsType(arg.getType())) {
                SemanticRepresentation newBody = lambda.getFuncBody();
                newBody = newBody.applyTo(lambda.getBinder(), arg);
                //newBody = newBody.betaReduce();
                if (arg != this.argument)
                    return new FuncApp(newBody, this.argument).betaReduce();
                else
                    return newBody.betaReduce();
            }
        }
        else if (this.functor instanceof SemQuantEx) {
            SemQuantEx quant = (SemQuantEx) this.functor;
            if (quant.getBinder().getType().equalsType(arg.getType())) {
                SemanticRepresentation newBody = quant.getQuantBody();
                newBody = newBody.applyTo(quant.getBinder(), arg);
                //newBody = newBody.betaReduce();
                if (arg != this.argument)
                    return new FuncApp(newBody, this.argument);
                    //return new FuncApp(newBody,arg);
                else
                    return newBody.betaReduce();
            }
        }
        else if (this.functor instanceof FuncApp) {
           return ((FuncApp) this.functor).apply(arg);
          //  return this;
        }
        else if (this.functor instanceof MeaningRepresentation) {

            if (arg.equals(this.argument))
                return this;
            return new FuncApp(this,arg);
         //   return new MeaningRepresentation(String.format("app(%s,%s)",functor.toString(),arg.toString()));
        }
        return this;
    }


  /*
    public SemanticRepresentation checkBinder(SemanticRepresentation arg)
    {

        Set<String> possibleVariables = new HashSet<>();

        SemanticRepresentation functor = this;

        if (arg instanceof FuncApp)
        {
            while (functor instanceof FuncApp)
            {
                if (((FuncApp) functor).argument instanceof SemAtom)
                {
                    possibleVariables.add(SemAtom);
                }


                if (!(((FuncApp) functor).functor instanceof FuncApp))
                {
                    break;
                }

                functor = ((FuncApp) functor).functor;
            }


        } else if (arg instanceof SemPred)
        {

        }
    }
    */

    /*
    This method is only called when this object is the body of a SemFunc
    which amounts to two cases ( [] = functor, () = argument):
    a) [LP. Lx. P(x)]  (Ly.predicate(y))
    b) [Lu. LP. P  (Lv.u)] (predicate(v)))
    In the first case we want to substitute P for the argument and then betaReduce.
    In the second case we want to apply the argument of this FuncApp to arg.
    Both cases can be done sequentially, as an unsuccessful application attempt
    simply returns the unsimplified expression.
    */
    public SemanticRepresentation applyTo(SemanticRepresentation var, SemanticRepresentation arg) throws ProverException {
            SemanticRepresentation appliedFunc = this.functor.applyTo(var, arg);
            if (appliedFunc instanceof FuncApp)
                appliedFunc = appliedFunc.betaReduce();
            SemanticRepresentation appliedArg = this.argument.applyTo(var, arg);

            return new FuncApp(appliedFunc,appliedArg);
    }

    @Override
    public SemanticExpression clone() {
        return new FuncApp(this);
    }

    @Override
    public SemType getType() {
        return functor.getType();
    }

    @Override
    public String toString() {
        if (LLProver.getSettings().getSemanticOutputStyle() == PROLOG)
            return String.format("app(%s,%s)",functor.toString(),argument.toString());
        else
            return functor + "(" + argument + ")";

    }

    @Override
    public void instantiateFunctionalApp(SemanticRepresentation func, SemanticRepresentation arg) {
        this.functor = func;
        this.argument = arg;
    }


    public void setFunctor(SemanticRepresentation functor) {
        this.functor = functor;
    }
}
