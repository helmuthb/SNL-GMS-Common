package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

@FunctionalInterface
interface TriFunction<A,B,C,R> {

  R apply(A a, B b, C c) throws Exception;
}
