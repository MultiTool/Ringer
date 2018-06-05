package ringer;

class Lab {// Laboratory
  /* ********************************************************************** */
  public static void Run_Test() {
    switch (0) {
      case 0:
        Test_Sounder();
        break;
    }
  }
  /* ********************************************************************** */
  static void Test_Sounder() {
    Org TopOrg = null;
    TopOrg = Evo_Sounder();
    TopOrg.Delete_Me();
  }
  /* ********************************************************************** */
  static Org Evo_Sounder() {// Evolve a vector to be the highest-energy wave
    Org TopOrg;
    int OrgLen = 64;//4;//16;//32;
    int PopSize = 100;
    System.out.printf("Evo_Sounder, OrgLen:%d\n", OrgLen);
    Tester SoundTester = new Tester_Sounder();
    SoundTester.Assign_Pop_Size(PopSize, OrgLen);
    Pop pop = new Pop();
    pop.Assign_Params(PopSize, OrgLen, SoundTester, /* MaxOrgGens */ 10000, 1, /* EvoStagnationLimit */ 30000);
    pop.Evolve();
    TopOrg = pop.CloneTopOrg();
    pop.Delete_Me();
    SoundTester.Delete_Me();
    return TopOrg;
  }
};
