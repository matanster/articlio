package com.articlio.LanguageModel

abstract class DomainProperty
abstract class SelfEntity extends DomainProperty
case object    SelfWork   extends SelfEntity
case object    SelfAuthor extends SelfEntity

//abstract class Annotated2 (annotated:Annotated, domainProperty:DomainProperty)
//case class AnnotatedAny2  (annotated:Annotated, domainProperty:DomainProperty) extends Annotated2 (annotated, domainProperty)

