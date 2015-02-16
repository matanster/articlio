package com.articlio.ldb

abstract class PlugType
case object    RefAppendable  extends PlugType // self reference is potentially *appendable* to target phrase
case object    RefPrependable extends PlugType // self reference is potentially *prependable* to target phrase

abstract class PotentiallyPluggable (val plugType: PlugType)
//case class AA extends PotentiallyPluggable(RefPrependable)
case object    VerbFragment extends PotentiallyPluggable(RefPrependable)
case object    NounFragment extends PotentiallyPluggable(RefPrependable)
case object    InByOfFragment   extends PotentiallyPluggable(RefAppendable)

//
// class hierarchy for describing rules as derived by the LDB object
//

abstract class Rule
case class SimpleRule (pattern: String, 
                       fragments: List[String], 
                       indication: String, 
                       locationProperty: Option[Seq[Property]], 
                       ReferenceProperty: Option[Seq[Property]]) 

case class ExpandedRule (rule: SimpleRule) extends Rule {
  def getFragmentType : PotentiallyPluggable = {
                          if (rule.pattern.containsSlice("{asr-V}")) return VerbFragment 
                          if (rule.pattern.containsSlice("{asr-N}")) return NounFragment
                          return InByOfFragment
  }

  val fragmentType = getFragmentType
  override def toString = s"$rule, $fragmentType"
}