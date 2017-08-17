package com.kategory.ank.listkw

import com.kategory.ank.AnkOps
import com.kategory.ank.AnkOpsHK
import kategory.*


@higherkind data class ListKW<A> constructor(val list: List<A>) : ListKWKind<A>, Collection<A> by list {

    fun <B> map(f: (A) -> B): ListKW<B> =
            ListKW(list.map(f))

    fun <B> flatMap(f: (A) -> ListKW<B>): ListKW<B> =
            ListKW(list.flatMap { f(it).list })

    operator fun plus(list: List<A>): ListKW<A> =
            ListKW(this.list + list)

    operator fun plus(listKW: ListKW<A>): ListKW<A> =
            ListKW(this.list + listKW.list)

    operator fun get(position: Int): Option<A> =
            if (list.isEmpty() || position < 0 || position > list.size) Option.None else Option.Some(list[position])

    fun <B> fold(b: B, f: (B, A) -> B): B =
            list.fold(b, f)

    companion object : ListKWInstances, GlobalInstance<Monad<ListKWHK>>() {

        @JvmStatic fun <A> listOfK(vararg a: A): ListKW<A> = ListKW(a.asList())
        @JvmStatic fun <A> listOfK(list: List<A>): ListKW<A> = ListKW(list)

        fun functor(): Functor<ListKWHK> = this

        fun applicative(): Applicative<ListKWHK> = this

        fun monad(): Monad<ListKWHK> = this

        fun <A> semigroup(): Semigroup<ListKW<A>> = object : ListKWMonoid<A> {}

        fun semigroupK(): SemigroupK<ListKWHK> = object : ListKWMonoidK {}

        fun <A> monoid(): ListKWMonoid<A> = object : ListKWMonoid<A> {}

        fun monoidK(): MonoidK<ListKWHK> = object : ListKWMonoidK {}

        fun traverse(): Traverse<ListKWHK> = this

    }

}

fun <A> List<A>.k(): ListKW<A> =
        ListKW.listOfK(this)

interface ListKWInstances :
        Functor<ListKWHK>,
        Applicative<ListKWHK>,
        Monad<ListKWHK>,
        Traverse<ListKWHK> {

    override fun <A> pure(a: A): ListKW<A> =
            ListKW.listOfK(a)

    override fun <A, B> flatMap(fa: HK<ListKWHK, A>, f: (A) -> HK<ListKWHK, B>): ListKW<B> =
            fa.ev().flatMap { f(it).ev() }

    override fun <A, B> map(fa: HK<ListKWHK, A>, f: (A) -> B): HK<ListKWHK, B> =
            fa.ev().map(f)

    override fun <A, B, Z> map2(fa: HK<ListKWHK, A>, fb: HK<ListKWHK, B>, f: (Tuple2<A, B>) -> Z): HK<ListKWHK, Z> =
            fa.ev().flatMap { a -> fb.ev().map { b -> f(Tuple2(a, b)) } }

    override fun <A, B> foldL(fa: HK<ListKWHK, A>, b: B, f: (B, A) -> B): B =
            fa.ev().fold(b, f)


    override fun <A, B> foldR(fa: HK<ListKWHK, A>, lb: Eval<B>, f: (A, Eval<B>) -> Eval<B>): Eval<B> {
        fun loop(fa_p: ListKW<A>): Eval<B> = when {
            fa_p.isEmpty() -> lb
            else -> f(fa_p.first(), Eval.defer { loop(fa_p.drop(1).k()) })
        }
        return Eval.defer { loop(fa.ev()) }
    }

    override fun <G, A, B> traverse(fa: HK<ListKWHK, A>, f: (A) -> HK<G, B>, GA: Applicative<G>): HK<G, HK<ListKWHK, B>> =
            foldR(fa, Eval.always { GA.pure(ListKW.listOfK<B>()) }) { a, eval ->
                GA.map2Eval(f(a), eval) { ListKW.listOfK(it.a) + it.b }
            }.value()


    override fun <A, B> tailRecM(a: A, f: (A) -> HK<ListKWHK, Either<A, B>>): ListKW<B> =
            f(a).ev().flatMap {
                when (it) {
                    is Either.Left -> tailRecM(it.a, f)
                    is Either.Right -> pure(it.b)
                }
            }
}

interface ListKWMonoid<A> : Monoid<ListKW<A>> {
    override fun combine(a: ListKW<A>, b: ListKW<A>): ListKW<A> =
            a + b
    override fun empty(): ListKW<A> =
            ListKW.listOfK()
}

interface ListKWMonoidK : MonoidK<ListKWHK> {
    override fun <A> combineK(x: HK<ListKWHK, A>, y: HK<ListKWHK, A>): ListKW<A> =
            x.ev() + y.ev()

    override fun <A> empty(): HK<ListKWHK, A> =
            ListKW.listOfK()
}

fun <A> ListKW<Free<AnkOpsHK, A>>.sequence(): Free<AnkOpsHK, ListKW<A>> =
        object : ListKWInstances { }.sequence(this, AnkOps).ev().map { it.ev() }