
# WireJark

WireJark, Java programlama diliyle geliştirilen, ağ yöneticileri ve güvenlik uzmanları için tasarlanmış bir ağ izleme ve analiz aracıdır. Sistem üzerinde ağ arayüzlerini listeleyerek, belirli IP adresleri üzerinde açık veya kapalı portları tespit eder ve açık portlar üzerinden gelen ve giden paketleri gerçek zamanlı olarak izler. Paralel programlama tekniklerini kullanarak port tarama ve paket analiz süreçlerini hızlandırır, çok çekirdekli mimariden yararlanarak işlemleri Runnable interfacesi ve Thread sınıfı ile gerçekleştirir. Port tarama işlemleri, paralel thread'ler aracılığıyla belirli aralıklara bölünerek eşzamanlı olarak taranır. Paket izleme işlemi, Runnable sınıfını implement eden ayrı thread'ler kullanılarak gerçek zamanlı olarak yönetilir ve analiz edilir. Ayrıca, büyük veri setlerinin işlenmesi ve görevlerin yönetimi için ThreadPoolExecutor kullanılarak, iş parçacıkları etkili bir şekilde kontrol edilir ve eşzamanlı işlemler gerçekleştirilir. Bu sayede, WireJark, yüksek performanslı, hızlı ve verimli bir ağ izleme ve analiz deneyimi sunar.


